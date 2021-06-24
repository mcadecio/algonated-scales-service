package com.dercio.algonated_scales_service.verticles;

import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.runner.CodeOptions;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.SCALES_VERTICLE;

@Slf4j
public class ScalesVerticle extends ConsumerVerticle {

    private MessageConsumer<CodeOptions> consumer;

    @Override
    public void start(Promise<Void> startPromise) {
        var eventBus = vertx.eventBus();
        consumer = eventBus.consumer(SCALES_VERTICLE.toString());
        consumer.handler(message -> eventBus.<Response>request(
                VerticleAddresses.CODE_RUNNER_CONSUMER.toString(),
                message.body(),
                reply -> {
                    if (reply.succeeded()) {
                        message.reply(reply.result().body());
                    } else {
                        message.fail(503, reply.cause().getMessage());
                    }
                }));
        consumer.completionHandler(result -> logRegistration(startPromise, result));
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        consumer.unregister(result -> logUnregistration(stopPromise, result));
    }

    @Override
    public String getAddress() {
        return SCALES_VERTICLE.toString();
    }
}
