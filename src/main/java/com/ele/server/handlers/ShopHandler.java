package com.ele.server.handlers;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import com.ele.data.repositories.ShopRepository;
import com.ele.model.dto.ele.Promotion;
import com.ele.model.dto.ele.PromotionType;
import com.ele.model.dto.ele.ShopProfile;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopHandler extends ApiHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ShopHandler.class);
    private final ShopRepository repo;

    public ShopHandler(Vertx vertx, ActorSystem system, ShopRepository shopRepo) {
        super(vertx, system);
        this.repo = shopRepo;
    }

    @Override
    public Router createSubRouter() {
        Router subRouter = Router.router(vertx);

        subRouter.get("/").handler(this::handleGetShop);
        repo.create(ShopProfile.newBuilder()
                .setId(UUID.randomUUID().toString())
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
                .addShopActivity(Promotion.newBuilder()
                        .setVariety(PromotionType.NEW)
                        .setSlogan("New User Deduct $17.0")
                        .build())
                .build()).whenComplete((result, error) -> {
                   if(error != null) {
                       LOG.error("Error creating a new row", error);
                   } else {
                       LOG.info("Create a new row");
                   }
                });

        repo.getAll().whenComplete((result, error) -> {
            if (error != null) {
                LOG.error("Error getting all rows");
            } else {
                LOG.info("Successful get all rows");
            }

        });
        return subRouter;
    }

    private void handleGetShop(RoutingContext context) {
        Source<ShopProfile, NotUsed> source = Source.from(mockShopList());
        serializationProcedure(context, source);
    }

    private List<ShopProfile> mockShopList() {

        List<ShopProfile> shoplist = new ArrayList<>();

        shoplist.add(ShopProfile.newBuilder()
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

        shoplist.add(ShopProfile.newBuilder()
                .setImgUrl("http://localhost:4000/apis/img/tan.jpg")
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

        shoplist.add(ShopProfile.newBuilder()
                .setImgUrl("http://localhost:4000/apis/img/tarjan.jpg")
                .setIsBrand(true)
                .setShopName("Test 3")
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

        shoplist.add(ShopProfile.newBuilder()
                .setImgUrl("http://localhost:4000/apis/img/tan.jpg")
                .setShopName("Test 4")
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

        shoplist.add(ShopProfile.newBuilder()
                .setImgUrl("http://localhost:4000/apis/img/tan.jpg")
                .setShopName("Test 5")
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

        shoplist.add(ShopProfile.newBuilder()
                .setImgUrl("http://localhost:4000/apis/img/tarjan.jpg")
                .setShopName("Test 6")
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

        shoplist.add(ShopProfile.newBuilder()
                .setImgUrl("http://localhost:4000/apis/img/tarjan.jpg")
                .setShopName("Test 7")
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

        shoplist.add(ShopProfile.newBuilder()
                .setImgUrl("http://localhost:4000/apis/img/tarjan.jpg")
                .setShopName("Test 8")
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
