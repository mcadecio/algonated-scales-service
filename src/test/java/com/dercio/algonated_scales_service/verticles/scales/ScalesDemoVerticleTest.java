package com.dercio.algonated_scales_service.verticles.scales;

import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.verticles.VerticleAddresses;
import com.dercio.algonated_scales_service.verticles.codec.CodecRegisterVerticle;
import com.dercio.algonated_scales_service.verticles.runner.demo.DemoOptions;
import com.dercio.algonated_scales_service.verticles.runner.demo.DemoRunnerVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class ScalesDemoVerticleTest {

    private static final Vertx vertx = Vertx.vertx();

    private static Function<DemoOptions, Response> mockedBehavior;

    @BeforeAll
    public static void prepare(VertxTestContext testContext) {
        vertx.deployVerticle(new CodecRegisterVerticle());
        vertx.deployVerticle(new DemoRunnerVerticle() {
            @Override
            public void start(Promise<Void> startPromise) {
                vertx.eventBus()
                        .<DemoOptions>consumer(getAddress())
                        .handler(message ->
                                message.reply(mockedBehavior.apply(message.body())));
            }

            @Override
            public void stop(Promise<Void> stopPromise) {
                // do nothing
            }

        });
        vertx.deployVerticle(
                new ScalesDemoVerticle(),
                testContext.succeeding(id -> testContext.completeNow())
        );
    }

    @Test
    @DisplayName("Returns a successful response")
    void returnSuccessfulResponse(VertxTestContext testContext) {
        var request = DemoOptions.builder()
                .algorithm("simulated annealing")
                .build();

        mockedBehavior = options -> new Response()
                .setSuccess(true)
                .setConsoleOutput(options.getAlgorithm());

        vertx.eventBus().<Response>request(
                VerticleAddresses.SCALES_DEMO.toString(),
                request,
                messageReply -> testContext.verify(() -> {
                    var response = messageReply.result().body();
                    assertEquals(request.getAlgorithm(), response.getConsoleOutput());
                    assertTrue(response.isSuccess());
                    testContext.completeNow();
                })
        );

    }

    @Test
    @DisplayName("Returns a unsuccessful response")
    void returnUnsuccessfulResponse(VertxTestContext testContext) {
        var request = DemoOptions.builder()
                .algorithm("simulated annealing")
                .build();

        mockedBehavior = options -> new Response()
                .setSuccess(false)
                .setConsoleOutput("Error");

        vertx.eventBus().<Response>request(
                VerticleAddresses.SCALES_DEMO.toString(),
                request,
                messageReply -> testContext.verify(() -> {
                    var response = messageReply.result().body();
                    assertFalse(response.isSuccess());
                    assertEquals("Error", response.getConsoleOutput());
                    testContext.completeNow();
                })
        );

    }

    @AfterAll
    public static void cleanup() {
        vertx.close();
    }

}