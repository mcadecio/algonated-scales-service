package com.dercio.algonated_scales_service.verticles;

import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.runner.CodeOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.SCALES_VERTICLE;

@Slf4j
public class ScalesVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        var eventBus = vertx.eventBus();
        eventBus
                .<CodeOptions>consumer(SCALES_VERTICLE.toString())
                .handler(message -> eventBus.<Response>request(
                        VerticleAddresses.CODE_RUNNER_CONSUMER.toString(),
                        message.body(),
                        reply -> {
                            if (reply.succeeded()) {
                                message.reply(reply.result().body());
                            } else {
                                message.fail(503, reply.cause().getMessage());
                            }
                        }));
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        stopPromise.complete();
    }
}
