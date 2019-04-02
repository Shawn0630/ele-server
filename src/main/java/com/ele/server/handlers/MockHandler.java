package com.ele.server.handlers;

import akka.actor.ActorSystem;
import com.ele.data.repositories.MockStorage;
import com.ele.data.repositories.file.ResultRepository;
import com.google.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

public class MockHandler extends ApiHandler {

    ResultRepository repo;
    private static final Logger LOG = LoggerFactory.getLogger(MockHandler.class);

    @Inject
    public MockHandler(Vertx vertx, ActorSystem system, MockStorage mockStorage) {
        super(vertx, system);
        this.repo = mockStorage.resultRepository();
    }

    @Override
    public Router createSubRouter() {
        Router subRouter = Router.router(vertx);

        subRouter.get("/denovo").handler(this::handleGetDenovoResult);

        return subRouter;
    }


    private void handleGetDenovoResult(RoutingContext context) {
        CompletableFuture.completedFuture(repo.denovo()).whenComplete((result, error) -> {
            if (error != null) {
                context.fail(error);
            } else {
                serializationProcedure(context, result);
            }
        });

    }

}
