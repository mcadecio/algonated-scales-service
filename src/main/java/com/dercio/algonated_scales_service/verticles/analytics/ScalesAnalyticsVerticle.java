package com.dercio.algonated_scales_service.verticles.analytics;

import com.dercio.algonated_scales_service.verticles.analytics.calculator.Calculator;
import com.dercio.algonated_scales_service.verticles.analytics.calculator.ScalesEfficiencyCalculator;
import com.dercio.algonated_scales_service.verticles.analytics.calculator.ScalesFitnessCalculator;
import com.dercio.algonated_scales_service.verticles.ConsumerVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.SCALES_ANALYTICS_SUMMARY;

@Slf4j
public class ScalesAnalyticsVerticle extends ConsumerVerticle {

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
        consumer = vertx.eventBus().consumer(getAddress());
        consumer.handler(message -> {
            log.info("Consuming message");
            var request = message.body();
            message.reply(createSummary(request));
        }).completionHandler(result -> logRegistration(startPromise, result));
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        consumer.unregister(result -> logUnregistration(stopPromise, result));
    }

    @Override
    public String getAddress() {
        return SCALES_ANALYTICS_SUMMARY.toString();
    }

    private CodeRunnerSummary createSummary(AnalyticsRequest request) {
        var summary = new CodeRunnerSummary();
        summary.setIterations(request.getIterations());
        summary.setTimeRun(request.getTimeElapsed());
        summary.setEfficacy(efficiencyCalculator.calculate(request.getWeights(), request.getSolution()));
        summary.setFitness(fitnessCalculator.calculate(request.getWeights(), request.getSolution()));
        return summary;
    }

}
