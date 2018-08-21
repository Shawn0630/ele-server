package com.ele.server.handlers;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class VarietyHandler extends ApiHandler {

    @Inject
    public VarietyHandler(Vertx vertx, ActorSystem system) {
        super(vertx, system);
    }

    @Override
    public Router createSubRouter() {
        Router subRouter = Router.router(vertx);

        subRouter.get("/").handler(this::handleGetVariety);

        return subRouter;
    }

    private void handleGetVariety(RoutingContext context) {
        ok(context, "variety");
    }
}
