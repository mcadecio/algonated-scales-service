package com.dercio.algonated_scales_service.config;

import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class HttpConfig {

    public Router configureRouter(Router router) {
        router.route().handler(BodyHandler.create());
        router.route().handler(createDefaultCorsHandler());
        return router;
    }

    public Handler<RoutingContext> createDefaultCorsHandler() {
        return CorsHandler.create(getAllowedDomain())
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type");
    }

    public String getAllowedDomain() {
        return System.getProperty("cors.allowed.domain", ".*://localhost:.*");
    }

    public int getPort() {
        return Integer.parseInt(System.getProperty("heroku.port", "80"));
    }

    public String getHost() {
        return System.getProperty("server.host", "0.0.0.0");
    }

}
