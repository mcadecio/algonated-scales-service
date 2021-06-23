package com.dercio.algonated_scales_service.verticles.analytics;

import com.dercio.algonated_scales_service.runner.CodeRunnerSummary;
import com.dercio.algonated_scales_service.runner.calculator.Calculator;
import com.dercio.algonated_scales_service.runner.calculator.ScalesEfficiencyCalculator;
import com.dercio.algonated_scales_service.runner.calculator.ScalesFitnessCalculator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.SCALES_ANALYTICS_SUMMARY;

@Slf4j
public class ScalesAnalyticsVerticle extends AbstractVerticle {

    private MessageConsumer<AnalyticsRequest> consumer;
    private final Calculator<List<Double>> efficiencyCalculator;
    private final Calculator<List<Double>> fitnessCalculator;

    public ScalesAnalyticsVerticle() {
        this(
                new ScalesEfficiencyCalculator(),
                new ScalesFitnessCalculator()
        );
    }

    ScalesAnalyticsVerticle(
            Calculator<List<Double>> efficiencyCalculator,
            Calculator<List<Double>> fitnessCalculator) {
        this.efficiencyCalculator = efficiencyCalculator;
        this.fitnessCalculator = fitnessCalculator;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        consumer = vertx.eventBus().consumer(SCALES_ANALYTICS_SUMMARY.toString());

        consumer.handler(message -> {
            log.info("Consuming message");
            var request = message.body();
            message.reply(createSummary(request));
        });

        consumer.completionHandler(result -> {
            if (result.succeeded()) {
                log.info("Registered -{}- consumer", SCALES_ANALYTICS_SUMMARY);
            } else {
                log.info("Failed to register -{}- consumer", SCALES_ANALYTICS_SUMMARY);
            }
            startPromise.complete();
        });
    }

    private CodeRunnerSummary createSummary(AnalyticsRequest request) {
        var summary = new CodeRunnerSummary();
        summary.setIterations(request.getIterations());
        summary.setTimeRun(request.getTimeElapsed());
        summary.setEfficacy(efficiencyCalculator.calculate(request.getWeights(), request.getSolution()));
        summary.setFitness(fitnessCalculator.calculate(request.getWeights(), request.getSolution()));
        return summary;
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        consumer.unregister(result -> {
            if (result.succeeded()) {
                log.info("UnRegistered -{}- consumer", SCALES_ANALYTICS_SUMMARY);
            } else {
                log.info("Failed to unregister -{}- consumer", SCALES_ANALYTICS_SUMMARY);
            }
            stopPromise.complete();
        });
    }

}
