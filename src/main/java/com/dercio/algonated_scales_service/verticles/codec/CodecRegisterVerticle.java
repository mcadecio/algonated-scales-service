package com.dercio.algonated_scales_service.verticles.codec;

import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.verticles.runner.CodeOptions;
import com.dercio.algonated_scales_service.verticles.analytics.CodeRunnerSummary;
import com.dercio.algonated_scales_service.verticles.analytics.AnalyticsRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodecRegisterVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus()
                .registerDefaultCodec(
                        AnalyticsRequest.class,
                        new GenericCodec<>(AnalyticsRequest.class))
                .registerDefaultCodec(
                        CodeRunnerSummary.class,
                        new GenericCodec<>(CodeRunnerSummary.class))
                .registerDefaultCodec(
                        CodeOptions.class,
                        new GenericCodec<>(CodeOptions.class))
                .registerDefaultCodec(
                        Response.class,
                        new GenericCodec<>(Response.class));
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        vertx.eventBus()
                .unregisterDefaultCodec(AnalyticsRequest.class)
                .unregisterDefaultCodec(CodeRunnerSummary.class)
                .unregisterDefaultCodec(CodeOptions.class)
                .unregisterDefaultCodec(Response.class);
        stopPromise.complete();
    }
}
