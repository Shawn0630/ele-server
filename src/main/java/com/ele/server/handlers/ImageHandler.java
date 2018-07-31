package com.ele.server.handlers;

import akka.actor.ActorSystem;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageHandler extends ApiHandler{

    private static final Logger LOG = LoggerFactory.getLogger(ImageHandler.class);
    private static final String root = "img";
    public ImageHandler(Vertx vertx, ActorSystem system) {
        super(vertx, system);
    }

    @Override
    public Router createSubRouter() {
        Router subRouter = Router.router(vertx);

        subRouter.get("/:filename").handler(this::handleGetImage);

        return subRouter;
    }


    private void handleGetImage(RoutingContext context) {
        String filename = context.request().getParam("filename");
        if (filename == null) {
            badRequest(context, "Bad Request");
            LOG.error("Bad Request from client");
            return;
        }
        sendFile(context, root + "/" + filename);
    }


}
