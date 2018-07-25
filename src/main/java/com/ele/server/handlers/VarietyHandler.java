package com.ele.server.handlers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class VarietyHandler extends ApiHandler {

    public VarietyHandler(Vertx vertx) {
        super(vertx);
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
