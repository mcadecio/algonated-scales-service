package com.dercio.algonated_scales_service.verticles.scales;

import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.verticles.ConsumerVerticle;
import com.dercio.algonated_scales_service.verticles.VerticleAddresses;
import com.dercio.algonated_scales_service.verticles.runner.demo.DemoOptions;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

import static com.dercio.algonated_scales_service.verticles.VerticleAddresses.SCALES_DEMO;

@Slf4j
public class ScalesDemoVerticle extends ConsumerVerticle {

    private MessageConsumer<DemoOptions> consumer;

    @Override
    public void start(Promise<Void> startPromise) {
        var eventBus = vertx.eventBus();
        consumer = eventBus.consumer(getAddress());
        consumer.handler(message -> eventBus.<Response>request(
                VerticleAddresses.DEMO_RUNNER_CONSUMER.toString(),
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
        return SCALES_DEMO.toString();
    }
}
