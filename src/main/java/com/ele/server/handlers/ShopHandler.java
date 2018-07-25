package com.ele.server.handlers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ShopHandler extends ApiHandler{

    public ShopHandler(Vertx vertx) {
        super(vertx);
    }

    @Override
    public Router createSubRouter() {
        Router subRouter = Router.router(vertx);

        subRouter.get("/").handler(this::handleGetShop);

        return subRouter;
    }

    private void handleGetShop(RoutingContext context) {
        ok(context, "shop");
    }
}
