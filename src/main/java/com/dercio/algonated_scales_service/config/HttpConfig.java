package com.dercio.algonated_scales_service.config;

import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class HttpConfig {

    public Router configureRouter(Router router) {
        router.route().handler(BodyHandler.create());
        router.route().handler(createDefaultCorsHandler());
        return router;
    }

    public Handler<RoutingContext> createDefaultCorsHandler() {
        return CorsHandler.create()
                .addOrigins(getAllowedDomains())
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type");
    }

    public List<String> getAllowedDomains() {
        log.info("{}", System.getProperties());
        var allowedDomains = Stream.of(System.getProperty("cors.allowed.domain", ".*://localhost:.*"))
                .map(string -> string.split(","))
                .flatMap(Stream::of)
                .collect(Collectors.toList());
        log.info("Allowed domains - {}", allowedDomains);
        return allowedDomains;
    }

    public int getPort() {
        var port = Integer.parseInt(System.getProperty("heroku.port", "80"));
        log.info("Port - {}", port);
        return port;
    }

    public String getHost() {
        var host = System.getProperty("server.host", "0.0.0.0");
        log.info("Host - {}", host);
        return host;
    }

}
