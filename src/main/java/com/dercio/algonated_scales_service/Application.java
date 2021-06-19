package com.dercio.algonated_scales_service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application extends AbstractVerticle {

    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) {
        final var router = Router.router(vertx);

        router.route().handler(rc -> {
            log.info("Received request on --> {} from --> {}", rc.request().path(), rc.request().host());
            rc.response().setChunked(true);
            rc.next();
        });

        router.get().handler(rc -> {
            rc.response().write("Hello, World!");
            rc.next();
        });

        router.get().handler(rc -> {
            log.info("Dispatched response to --> {}", rc.request().host());
            rc.response().end();
        });

        router.route().failureHandler(event ->
                event.response()
                        .setStatusCode(event.statusCode())
                        .end());


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

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Application());
    }
}
