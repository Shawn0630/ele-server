package com.ele.server.dependency;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class GuiceVertxDeploymentManager {
    private final Vertx vertx;

    public GuiceVertxDeploymentManager(final Vertx vertx) {
        this.vertx = vertx;
    }

    public void deployVerticle(final Class verticleClass) {
        deployVerticle(verticleClass, new DeploymentOptions());
    }

    public void deployVerticle(final Class verticleClass, final DeploymentOptions options) {
        vertx.deployVerticle(getFullVerticleName(verticleClass), options);
    }

    public void deployVerticle(final Class verticleClass, final DeploymentOptions options, Handler<AsyncResult<String>> completionHandler) {
        vertx.deployVerticle(getFullVerticleName(verticleClass), options, completionHandler);
    }

    private String getFullVerticleName(final Class verticleClass) {
        return GuiceVerticleFactory.PREFIX + ":" + verticleClass.getCanonicalName();
    }


}
