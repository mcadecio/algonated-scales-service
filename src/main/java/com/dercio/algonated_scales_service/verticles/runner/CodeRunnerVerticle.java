package com.dercio.algonated_scales_service.verticles.runner;

import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.runner.CodeOptions;
import com.dercio.algonated_scales_service.runner.CodeRunnerSummary;
import com.dercio.algonated_scales_service.runner.executor.SelfClosingExecutor;
import com.dercio.algonated_scales_service.verifier.IllegalMethodVerifier;
import com.dercio.algonated_scales_service.verifier.ImportVerifier;
import com.dercio.algonated_scales_service.verifier.VerifyResult;
import com.dercio.algonated_scales_service.verticles.analytics.AnalyticsRequest;
import com.google.common.base.Stopwatch;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;
import org.joor.ReflectException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.CODE_RUNNER_CONSUMER;
import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.SCALES_ANALYTICS_SUMMARY;

@Slf4j
public class CodeRunnerVerticle extends AbstractVerticle {

    private static final String PLEASE_REMOVE = "Please remove the following ";

    private MessageConsumer<CodeOptions> consumer;

    @Override
    public void start(Promise<Void> startPromise) {
        consumer = vertx.eventBus().consumer(CODE_RUNNER_CONSUMER.toString());
        consumer
                .handler(this::handleMessage)
                .completionHandler(result -> {
                    if (result.succeeded()) {
                        log.info("Registered -{}- consumer", CODE_RUNNER_CONSUMER);
                    } else {
                        log.info("Failed to register -{}- consumer", CODE_RUNNER_CONSUMER);
                    }
                    startPromise.complete();
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        consumer.unregister(result -> {
            if (result.succeeded()) {
                log.info("UnRegistered -{}- consumer", CODE_RUNNER_CONSUMER);
            } else {
                log.info("Failed to unregister -{}- consumer", CODE_RUNNER_CONSUMER);
            }
            stopPromise.complete();
        });
    }


    private void handleMessage(Message<CodeOptions> message) {
        log.info("Consuming message");
        CodeOptions options = message.body();

        var verifyResult = verifyCode(options);
        if (!verifyResult.isSuccess()) {
            message.fail(400, verifyResult.getErrorMessage());
            return;
        }

        var compileResult = compile(options);
        if (!compileResult.isSuccess()) {
            message.fail(400, compileResult.getErrorMessage());
            return;
        }

        var executionResult = execute(options, compileResult.getCompiledClass());
        if (!executionResult.isSuccess()) {
            message.fail(400, executionResult.getErrorMessage());
            return;
        }

        var analyticsRequest = AnalyticsRequest.builder()
                .solution(executionResult.getSolution())
                .iterations(options.getIterations())
                .timeElapsed(executionResult.getTimeElapsed())
                .weights(options.getWeights())
                .build();

        vertx.eventBus().<CodeRunnerSummary>request(
                SCALES_ANALYTICS_SUMMARY.toString(),
                analyticsRequest,
                reply -> {
                    if (reply.succeeded()) {
                        var codeRunnerSummary = reply.result().body();
                        message.reply(new Response()
                                .setSuccess(true)
                                .setConsoleOutput(executionResult.getErrorMessage())
                                .setResult(executionResult.getSolution())
                                .setData(options.getWeights())
                                .setSummary(codeRunnerSummary)
                                .setSolutions(executionResult.getSolutions()));
                    } else {
                        message.fail(400, reply.cause().getMessage());
                    }
                }
        );
    }

    public CompileResult compile(CodeOptions options) {
        try {
            var compiledClass = compileClass(options);
            return CompileResult.builder()
                    .compiledClass(compiledClass)
                    .errorMessage("")
                    .isSuccess(true)
                    .build();
        } catch (ReflectException reflectException) {
            return CompileResult.builder()
                    .isSuccess(false)
                    .errorMessage(reflectException.getMessage())
                    .build();
        }
    }

    public ExecutionResult execute(CodeOptions options, Reflect compiledClass) {
        String errorMessage;
        try {
            var timer = Stopwatch.createStarted();
            String rawSolution = compiledClass.call(options.getMethodToCall(), options.getWeights(), options.getIterations())
                    .get();
            timer.stop();
            List<String> stringSolutions = compiledClass.get("solutions");

            var solutions = binaryStringToList(stringSolutions);
            var solution = transformStringToList(rawSolution);
            return ExecutionResult.builder()
                    .solution(solution)
                    .solutions(solutions)
                    .isSuccess(true)
                    .errorMessage("Compile and Run was a success")
                    .timeElapsed(timer.elapsed(TimeUnit.MILLISECONDS))
                    .build();
        } catch (ReflectException | ExecutionException exception) {
            errorMessage = exception.getMessage();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            errorMessage = interruptedException.getMessage();
        }

        return ExecutionResult.builder()
                .errorMessage(errorMessage)
                .isSuccess(false)
                .build();
    }

    private VerifyResult verifyCode(CodeOptions options) {
        List<String> importsFound = new ImportVerifier(options.getImportsAllowed())
                .verify(options.getCode());
        if (!importsFound.isEmpty()) {
            return VerifyResult.builder()
                    .isSuccess(false)
                    .errorMessage(PLEASE_REMOVE + "imports:\n" + importsFound)
                    .build();
        }

        List<String> illegalMethods = new IllegalMethodVerifier(options.getIllegalMethods())
                .verify(options.getCode());
        if (!illegalMethods.isEmpty()) {
            return VerifyResult.builder()
                    .isSuccess(false)
                    .errorMessage(PLEASE_REMOVE + "illegal methods:\n" + illegalMethods)
                    .build();
        }

        return VerifyResult.builder()
                .isSuccess(true)
                .build();
    }

    private Reflect compileClass(CodeOptions options) {
        String packageName = "package " + options.getPackageName() + ";";
        String className = options.getPackageName() + "." + options.getClassName();
        return Reflect.compile(className, packageName +
                "\n" +
                String.join("\n", options.getImportsAllowed()) +
                "\n" +
                options.getCode()
        ).create();
    }

    private List<Integer> transformStringToList(String solution) {
        return Stream.of(solution.split(""))
                .filter(s -> !s.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<List<Integer>> binaryStringToList(List<String> rawSolutions) throws InterruptedException, ExecutionException {
        if (rawSolutions.size() < 100) {
            return rawSolutions.stream()
                    .map(this::transformStringToList)
                    .collect(Collectors.toList());
        }

        var nPartitions = 10.0;

        List<List<String>> batches = createBatches(nPartitions, rawSolutions);

        return runBatches(batches, nPartitions);
    }

    private List<String> getListFromTo(List<String> list, int counter, int size) {
        List<String> strings = new ArrayList<>();
        for (int i = counter; i < counter + size; i++) {
            strings.add(list.get(i));
        }
        return strings;
    }


    private Callable<List<List<Integer>>> toCallableTask(List<String> batch) {
        return () -> batch.stream()
                .map(this::transformStringToList)
                .collect(Collectors.toList());
    }

    private List<List<String>> createBatches(double nPartitions, List<String> rawSolutions) {
        int batchNumber = (int) Math.ceil(rawSolutions.size() / nPartitions);
        var counter = 0;

        List<List<String>> batches = new ArrayList<>();
        for (var i = 0; i < nPartitions - 1; i++) {
            batches.add(getListFromTo(rawSolutions, counter, batchNumber));
            counter = counter + batchNumber;
        }
        batches.add(getListFromTo(rawSolutions, counter, rawSolutions.size() - counter));

        return batches;
    }

    private List<List<Integer>> runBatches(List<List<String>> batches, double nPartitions) throws InterruptedException, ExecutionException {
        List<List<Integer>> result = new ArrayList<>();
        try (var selfClosingExecutor = new SelfClosingExecutor((int) nPartitions)) {

            List<Future<List<List<Integer>>>> futures = selfClosingExecutor
                    .invokeAll(batches.stream()
                            .map(this::toCallableTask)
                            .collect(Collectors.toList()));

            for (Future<List<List<Integer>>> future : futures) {
                result.addAll(future.get());
            }
        }
        return result;
    }
}
