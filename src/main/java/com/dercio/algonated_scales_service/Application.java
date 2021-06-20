package com.dercio.algonated_scales_service;

import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.runner.CodeOptions;
import com.dercio.algonated_scales_service.runner.ScalesCodeRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Application extends AbstractVerticle {

    private final Gson gson = new Gson();
    private final ObjectMapper mapper = new ObjectMapper();
    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) {
        final var router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(rc -> {
            log.info("Received request on --> {} from --> {}", rc.request().path(), rc.request().host());
            rc.response().setChunked(true);
            rc.next();
        });

        router.get().handler(rc -> {
            rc.response().write("Hello, World!");
            rc.next();
        });

        router.post("/exercise/submit/scales")
                .handler(rc -> executeBlocking(() -> handleScalesRequest(rc), rc));

        router.route().handler(rc -> {
            log.info("Dispatched response to --> {}", rc.request().host());
            rc.response().end();
        });

        router.route().failureHandler(event -> {

            if (event.failure() instanceof NullPointerException) {
                event.response()
                        .setStatusCode(503)
                        .end("Internal Server Error: NullPointerException");
            } else {
                event.response()
                        .setStatusCode(event.statusCode())
                        .end();
            }
        });


        var port = Integer.parseInt(System.getProperty("heroku.port", "1234"));
        httpServer = vertx.createHttpServer();
        httpServer
                .requestHandler(router)
                .listen(port, "0.0.0.0", result -> {
                    log.info("HTTP Server Started ...");
                    startPromise.complete();
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        httpServer.close(event -> {
            log.info("Goodbye HTTP server ...");
            if (!event.succeeded()) {
                log.error(event.cause().getMessage());
            }
            stopPromise.complete();
        });
    }

    private void handleScalesRequest(RoutingContext rc) {
        Timer.runTimedTask(() -> {
            final String prettyRequest = rc.getBodyAsJson().encodePrettily();
            log.debug("This was the code submitted -> \n{}", prettyRequest);

            final var codeOptions = gson.fromJson(prettyRequest, CodeOptions.class);

            List<Double> data = gson.<List<Double>>fromJson(
                    rc.getBodyAsJson()
                            .getJsonArray("data")
                            .encode(),
                    List.class
            );

            sendResponse(rc, Timer.runTimedTask(
                    () -> new ScalesCodeRunner(codeOptions, data).compile().execute(),
                    "Code Runner"
            ).toResponse());

        }, "Scales Request");

    }

    private void executeBlocking(Runnable runnable, RoutingContext rc) {
        vertx.executeBlocking(promise -> {
            runnable.run();
            promise.complete();
        }, result -> {
            if (result.failed()) {
                rc.fail(result.cause());
            }
        });
    }

    private void sendResponse(RoutingContext rc, Response response) {
        String chunk = Timer.runTimedTaskWithException(
                () -> mapper.writeValueAsString(response),
                "Mapper timer",
                "{}"
        );

        rc.response()
                .putHeader("Content-type", "application/json")
                .write(chunk);
        rc.next();
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Application());
    }
}
