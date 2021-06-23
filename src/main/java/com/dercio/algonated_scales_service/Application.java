package com.dercio.algonated_scales_service;

import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.runner.CodeOptions;
import com.dercio.algonated_scales_service.verticles.CodecRegisterVerticle;
import com.dercio.algonated_scales_service.verticles.ScalesVerticle;
import com.dercio.algonated_scales_service.verticles.VerticleAddresses;
import com.dercio.algonated_scales_service.verticles.analytics.ScalesAnalyticsVerticle;
import com.dercio.algonated_scales_service.verticles.runner.CodeRunnerVerticle;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application extends AbstractVerticle {

    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) {
        final var router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // config
        router.route().handler(createCorsHandler()); // config
        router.route().handler(rc -> {
            log.info("Received request on --> {} from --> {}", rc.request().path(), rc.request().host());
            rc.response().setChunked(true);
            rc.next();
        });

        router.post("/exercise/submit/scales").handler(this::handleScalesRequest);

        router.route().handler(rc -> {
            log.info("Dispatched response to --> {}", rc.request().host());
            rc.response().end();
        });

        router.route().failureHandler(event -> {
            log.error("Error: ", event.failure());
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

        var port = Integer.parseInt(System.getProperty("heroku.port", "80"));
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
        vertx.eventBus().<Response>request(
                VerticleAddresses.SCALES_VERTICLE.toString(),
                rc.getBodyAsJson().mapTo(CodeOptions.class),
                reply -> rc.response()
                        .setChunked(true)
                        .putHeader("Content-type", "application/json")
                        .write(reply.result().body().encode())
                        .end()
        );
    }

    private String getAllowedDomain() {
        String allowedDomain = System.getProperty("cors.allowed.domain", ".*://localhost:.*");
        log.info(allowedDomain);
        return allowedDomain;
    }

    private Handler<RoutingContext> createCorsHandler() {
        return CorsHandler.create(getAllowedDomain())
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowCredentials(true)
                .allowedHeader("Authorization")
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type");
    }

    public static void main(String[] args) {
        var vertx = Vertx.vertx();
        var deployment = new DeploymentOptions();
        vertx.deployVerticle(new CodecRegisterVerticle(), deployment);
        vertx.deployVerticle(new ScalesVerticle(), deployment);
        vertx.deployVerticle(new CodeRunnerVerticle(), deployment);
        vertx.deployVerticle(new ScalesAnalyticsVerticle(), deployment);
        vertx.deployVerticle(new Application(), deployment);
    }
}
