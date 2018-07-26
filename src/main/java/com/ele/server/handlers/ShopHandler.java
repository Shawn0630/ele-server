package com.ele.server.handlers;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import com.ele.model.dto.ele.Shop;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class ShopHandler extends ApiHandler{
    private static final Logger LOG = LoggerFactory.getLogger(ShopHandler.class);

    public ShopHandler(Vertx vertx, ActorSystem system) {
        super(vertx, system);
    }

    @Override
    public Router createSubRouter() {
        Router subRouter = Router.router(vertx);

        subRouter.get("/").handler(this::handleGetShop);

        return subRouter;
    }

    private void handleGetShop(RoutingContext context) {
        Shop shop1 = Shop.newBuilder()
                .setDeliveryFee(123)
                .setDistance(123).build();
        Shop shop2 = Shop.newBuilder()
                .setDeliveryFee(456)
                .setDistance(456).build();
        List<Shop> shopList = new ArrayList<>();
        shopList.add(shop1);
        shopList.add(shop2);
        Source<Shop, NotUsed> source = Source.from(shopList);
        serializationProcedure(context, source);
    }
}
