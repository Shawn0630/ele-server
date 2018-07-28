package com.ele.server.handlers;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import com.ele.model.dto.ele.Promotion;
import com.ele.model.dto.ele.PromotionType;
import com.ele.model.dto.ele.Shop;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class ShopHandler extends ApiHandler {
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
        Source<Shop, NotUsed> source = Source.from(mockShopList());
        serializationProcedure(context, source);
    }

    private List<Shop> mockShopList() {

        List<Shop> shoplist = new ArrayList<>();

        shoplist.add(Shop.newBuilder()
                .setImgUrl("http://localhost:4000/apis/img/tarjan.jpg")
                .setIsBrand(true)
                .setShopName("Test 1")
                .setStarNum(4.6)
                .setMonthlySales(532)
                .setStarNum(5.0)
                .setDeliveryFee(5)
                .setDistance(1177.2)
                .setNeedTime("20H5min")
                .setIsBird(true)
                .setIsInsurance(true)
                .setNeedtip(true)
                .setIsNewShop(true)
                .addShopActivity(Promotion.newBuilder()
                        .setVariety(PromotionType.NEW)
                        .setSlogan("New User Deduct $17.0")
                        .build())
                .addShopActivity(Promotion.newBuilder()
                        .setVariety(PromotionType.SUBTRACTION)
                        .setSlogan("Deduct $20.0")
                        .build())
                .addShopActivity(Promotion.newBuilder()
                        .setVariety(PromotionType.SPECIAL)
                        .setSlogan("$16.0 Off")
                        .build())
                .addShopActivity(Promotion.newBuilder()
                        .setVariety(PromotionType.DISCOUNT)
                        .setSlogan("50% Off")
                        .build())
                .build());

        shoplist.add(Shop.newBuilder()
                .setImgUrl("http://images.cangdu.org/15c58db523471.jpg")
                .setShopName("Test 2")
                .setStarNum(4.9)
                .setMonthlySales(353)
                .setInitMoney(20)
                .setDeliveryFee(5)
                .setDistance(2125.6)
                .setNeedTime("31H22min")
                .setIsBird(true)
                .setIsOntime(true)
                .setNeedtip(true)
                .setIsNewShop(false)
                .build());

        return shoplist;
    }
}
