package com.ele.server;

import akka.actor.ActorSystem;
import com.ele.server.dependency.MasterDependency;
import com.ele.server.handlers.HandlerException;
import com.ele.server.handlers.ImageHandler;
import com.ele.server.handlers.ShopHandler;
import com.ele.server.handlers.VarietyHandler;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServerVerticle extends AbstractVerticle{

    private static final Logger LOG = LoggerFactory.getLogger(ServerVerticle.class);

    @Override
    public void start(final Future<Void> started) {
        Router router = Router.router(vertx);
        boolean allowCors = true;
        ActorSystem system = ActorSystem.apply("main");

        String root = "/apis";



        router.route().handler(BodyHandler.create());

        if (allowCors) {
            Set<HttpMethod> allowedMethods = new HashSet<>(Arrays.asList(
                    HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE
            ));

            Set<String> allowedHeaders = new HashSet<>(Arrays.asList(
                    "accept", "authorization", "content-type"
            ));

            router.route(root + "/*").handler(CorsHandler.create(".*")
                .allowedHeaders(allowedHeaders)
                .allowedMethods(allowedMethods)
                .allowCredentials(true)
            );
        }

        router.route(root + "/*").handler(context -> {
            context.response().putHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            context.response().putHeader("Pragma", "no-cache");
            context.response().putHeader("Expires", "0");
            context.next();
        });

        router.mountSubRouter(root + "/shop", new ShopHandler(vertx, system).createSubRouter());
        router.mountSubRouter(root + "/variety", new VarietyHandler(vertx, system).createSubRouter());
        router.mountSubRouter(root + "/img", new ImageHandler(vertx, system).createSubRouter());

        failureHandler(router.route(root + "/*"));

        vertx.createHttpServer().requestHandler(router::accept).listen(4000, result -> {
            if (result.succeeded()) {
                started.complete();
                LOG.info("Server started successfully at port " + 4000);
            } else {
                started.fail(result.cause());
                LOG.info("Server failed to start", result.cause());
            }
        });
    }


    public static void failureHandler(Route rootRoute) {
        rootRoute.failureHandler(context -> {
            final HttpServerRequest request = context.request();
            final String req;
            if (request == null) {
                req = "NULL request";
            } else {
                req = request.method() + " " + request.path();
            }

            Throwable error = context.failure();
            if (error == null) {
                LOG.error(req + " : Route failure NULL");
                context.response().setStatusCode(context.statusCode()).end();
                return;
            }
            for (Throwable throwable: Lists.reverse(Throwables.getCausalChain(error))) {
                if (throwable instanceof HandlerException) {
                    HandlerException he = (HandlerException) throwable;
                    LOG.error(req + " : " + he.getMessage(), he.getCause());
                    context.response().setStatusCode(he.getErrorCode()).end(he.getMessage());
                    return;
                }
            }
            LOG.error(req + " : " + error.getMessage());
            context.response().setStatusCode(500).end();
        });
    }
}
