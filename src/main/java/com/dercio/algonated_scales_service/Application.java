package com.dercio.algonated_scales_service;

import com.dercio.algonated_scales_service.config.HttpConfig;
import com.dercio.algonated_scales_service.response.Response;
import com.dercio.algonated_scales_service.runner.CodeOptions;
import com.dercio.algonated_scales_service.verticles.CodecRegisterVerticle;
import com.dercio.algonated_scales_service.verticles.ScalesVerticle;
import com.dercio.algonated_scales_service.verticles.VerticleAddresses;
import com.dercio.algonated_scales_service.verticles.analytics.ScalesAnalyticsVerticle;
import com.dercio.algonated_scales_service.verticles.runner.CodeRunnerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application extends AbstractVerticle {

    private static final String APPLICATION_JSON = "application/json";
    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) {
        var httpConfig = new HttpConfig();
        final var router = Router.router(vertx);
        httpConfig.configureRouter(router);

        router.post("/exercise/submit/scales")
                .handler(this::logRequestReceipt)
                .handler(this::handleScalesRequest)
                .handler(this::logResponseDispatch)
                .failureHandler(this::failureHandler);

        httpServer = vertx.createHttpServer();
        httpServer
                .requestHandler(router)
                .listen(httpConfig.getPort(), httpConfig.getHost(), result -> {
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

    private void logResponseDispatch(RoutingContext rc) {
        log.info("Dispatched response to --> {}", rc.request().host());
        rc.response().end();
    }

    private void logRequestReceipt(RoutingContext rc) {
        log.info("Received request on --> {} from --> {}", rc.request().path(), rc.request().host());
        rc.response().setChunked(true);
        rc.next();
    }

    private void failureHandler(RoutingContext event) {
        log.error("Error: {}", event.failure().getMessage());
        event.response()
                .setStatusCode(400)
                .putHeader("Content-type", APPLICATION_JSON)
                .end(new Response()
                        .setSuccess(false)
                        .setConsoleOutput(extractMessage(event.failure()))
                        .encode());
    }

    private String extractMessage(Throwable throwable) {
        if (throwable instanceof NullPointerException) {
            return "Internal Server Error: NullPointerException";
        } else {
            return throwable.getMessage();
        }
    }

    private void handleScalesRequest(RoutingContext rc) {
        vertx.eventBus().<Response>request(
                VerticleAddresses.SCALES_VERTICLE.toString(),
                rc.getBodyAsJson().mapTo(CodeOptions.class),
                reply -> {
                    if (reply.succeeded()) {
                        rc.response()
                                .putHeader("Content-Type", APPLICATION_JSON)
                                .write(reply.result().body().encode());
                        rc.next();
                    } else {
                        rc.fail(reply.cause());
                    }
                });
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
