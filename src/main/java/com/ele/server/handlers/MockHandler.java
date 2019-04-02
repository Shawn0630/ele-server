package com.ele.server.handlers;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import com.ele.data.repositories.MockStorage;
import com.ele.data.repositories.file.ResultRepository;
import com.example.dto.ScanDenovoCandidate;
import com.google.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;

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
        Source<ScanDenovoCandidate, NotUsed> denovo;
        try {
           denovo = repo.denovo();
        } catch (IOException e) {
            notFound(context);
            return;
        }
        serializationProcedure(context, denovo);

    }

}
