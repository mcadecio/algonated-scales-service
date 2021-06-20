package com.dercio.algonated_scales_service.verticles;

import com.dercio.algonated_scales_service.codec.GenericCodec;
import com.dercio.algonated_scales_service.runner.CodeRunnerSummary;
import com.dercio.algonated_scales_service.verticles.analytics.AnalyticsRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeRegisterVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus()
                .registerDefaultCodec(
                        AnalyticsRequest.class,
                        new GenericCodec<>(AnalyticsRequest.class))
                .registerDefaultCodec(
                        CodeRunnerSummary.class,
                        new GenericCodec<>(CodeRunnerSummary.class));
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        vertx.eventBus()
                .unregisterDefaultCodec(AnalyticsRequest.class)
                .unregisterDefaultCodec(CodeRunnerSummary.class);
        stopPromise.complete();
    }
}
