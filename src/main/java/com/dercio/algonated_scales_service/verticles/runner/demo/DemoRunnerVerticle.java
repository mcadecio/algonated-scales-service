package com.dercio.algonated_scales_service.verticles.runner.demo;

import com.dercio.algonated_scales_service.algorithms.Algorithm;
import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.verticles.ConsumerVerticle;
import com.dercio.algonated_scales_service.verticles.analytics.AnalyticsRequest;
import com.dercio.algonated_scales_service.verticles.analytics.CodeRunnerSummary;
import com.google.common.base.Stopwatch;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.DEMO_RUNNER_CONSUMER;
import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.SCALES_ANALYTICS_SUMMARY;

@Slf4j
public class DemoRunnerVerticle extends ConsumerVerticle {

    private MessageConsumer<DemoOptions> consumer;

    @Override
    public void start(Promise<Void> startPromise) {
        var eventBus = vertx.eventBus();
        consumer = eventBus.consumer(getAddress());
        consumer.handler(this::handleMessage);
        consumer.completionHandler(result -> logRegistration(startPromise, result));
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        consumer.unregister(result -> logUnregistration(stopPromise, result));
    }

    @Override
    public String getAddress() {
        return DEMO_RUNNER_CONSUMER.toString();
    }

    private void handleMessage(Message<DemoOptions> message) {
        var request = message.body();
        Algorithm algorithm = Algorithm.getScalesAlgorithm(request);

        Stopwatch timer = Stopwatch.createStarted();
        var solution = algorithm.run(request.getWeights(), request.getIterations())
                .getSolution();
        timer.stop();

        var analyticsRequest = AnalyticsRequest.builder()
                .solution(solution)
                .iterations(request.getIterations())
                .timeElapsed(timer.elapsed(TimeUnit.MILLISECONDS))
                .weights(request.getWeights())
                .build();

        vertx.eventBus().<CodeRunnerSummary>request(
                SCALES_ANALYTICS_SUMMARY.toString(),
                analyticsRequest,
                reply -> {
                    if (reply.succeeded()) {
                        var codeRunnerSummary = reply.result().body();
                        message.reply(new Response()
                                .setSuccess(true)
                                .setConsoleOutput("Your Demo is ready!")
                                .setResult(solution)
                                .setData(request.getWeights())
                                .setSummary(codeRunnerSummary)
                                .setSolutions(algorithm.getSolutions()));
                    } else {
                        log.error("Error: {}", reply.cause().getMessage());
                        message.fail(400, reply.cause().getMessage());
                    }
                }
        );
    }

}
