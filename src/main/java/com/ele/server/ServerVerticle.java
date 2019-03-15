package com.ele.server;

import akka.actor.ActorSystem;
import com.ele.data.repositories.SystemStorage;
import com.ele.server.config.SystemConfig;
import com.ele.server.dependency.MasterDependency;
import com.ele.server.handlers.FileHandler;
import com.ele.server.handlers.HandlerException;
import com.ele.server.handlers.ShopHandler;
import com.ele.server.handlers.VarietyHandler;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServerVerticle extends AbstractVerticle{

    private static final Logger LOG = LoggerFactory.getLogger(ServerVerticle.class);
    private final SystemConfig sysConfig;
    private final Injector injector;
    private final Vertx vertx;

    @Inject
    public ServerVerticle(final Vertx vertx, final Injector injector, final SystemConfig config) {
        this.vertx = vertx;
        this.injector = injector;
        this.sysConfig = config;
    }

    @Override
    public void start(final Future<Void> started) {
        Router router = Router.router(vertx);
        boolean allowCors = sysConfig.getAllowCore();
        ActorSystem system = ActorSystem.apply("main");
        String root = sysConfig.getApiRoot();
        int port = sysConfig.getServerHttpPort();



        //router.route().handler(BodyHandler.create());

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

        router.mountSubRouter(root + "/shop", injector.getInstance(ShopHandler.class).createSubRouter());
        router.mountSubRouter(root + "/variety", injector.getInstance(VarietyHandler.class).createSubRouter());
        router.mountSubRouter(root + "/img", injector.getInstance(FileHandler.class).createSubRouter());

        failureHandler(router.route(root + "/*"));

        if (sysConfig.isWebIntegration()) {
            LOG.info("Serving web content from Vertx");
            StaticHandler webHanlder = StaticHandler.create()
                    .setWebRoot("web")
                    .setIndexPage("index.html")
                    .setCachingEnabled(true)
                    .setCacheEntryTimeout(604800000)
                    .setDefaultContentEncoding("UTF-8")
                    .setFilesReadOnly(true);
            router.route("/*").handler(webHanlder);
            router.get().pathRegex("^(?!(" + root + ".*|bundle.js|bundle.css)$).*").handler(context ->
                    context.reroute("/index.html")
            );
        }

        vertx.createHttpServer().requestHandler(router::accept).listen(port, result -> {
            if (result.succeeded()) {
                started.complete();
                LOG.info("Server started successfully at port " + port);
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
