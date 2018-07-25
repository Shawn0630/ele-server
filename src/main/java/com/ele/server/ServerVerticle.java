package com.ele.server;

import com.ele.server.handlers.ShopHandler;
import com.oracle.tools.packager.Log;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ServerVerticle extends AbstractVerticle{

    private static final Logger LOG = LoggerFactory.getLogger(ServerVerticle.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ServerVerticle());
    }

    @Override
    public void start(final Future<Void> started) {
        Router router = Router.router(vertx);

        String root = "/apis";



        router.route().handler(BodyHandler.create());
        router.route(root + "/*").handler(context -> {
            context.response().putHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            context.response().putHeader("Pragma", "no-cache");
            context.response().putHeader("Expires", "0");
            context.next();
        });

        router.mountSubRouter(root + "/shop", new ShopHandler(vertx).createSubRouter());

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



    private void handleGetVariety(RoutingContext context) {
        System.out.println("variety");
    }

}
