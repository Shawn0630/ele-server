package com.ele.server;

import com.ele.data.repositories.MySQLStorage;
import com.ele.data.repositories.SystemStorage;
import com.ele.server.config.SystemConfig;
import com.ele.server.dependency.GuiceVerticleFactory;
import com.ele.server.dependency.GuiceVertxDeploymentManager;
import com.ele.server.dependency.MasterDependency;
import com.github.racc.tscg.TypesafeConfigModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Launcher {

    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        Injector injector;
        Config config = ConfigFactory.defaultApplication();
        config = config.withFallback(ConfigFactory.defaultReference());
        injector = Guice.createInjector(TypesafeConfigModule.fromConfigWithPackage(config, "com.ele"), new MasterDependency(config));

        startServerVerticle(injector);
    }

    private static void startServerVerticle(Injector injector) {
        Vertx vertx = injector.getInstance(Vertx.class);
        SystemConfig sysConfig = injector.getInstance(SystemConfig.class);
        GuiceVerticleFactory verticleFactory = new GuiceVerticleFactory(injector);
        vertx.registerVerticleFactory(verticleFactory);
        GuiceVertxDeploymentManager deploymentManager = new GuiceVertxDeploymentManager(vertx);
        DeploymentOptions options = new DeploymentOptions().setInstances(sysConfig.getServerInstances());
        deploymentManager.deployVerticle(ServerVerticle.class, options, result -> {
            if (result.succeeded()) {
                LOG.info("Server verticle successfully deployed");
            } else {
                LOG.error("Server verticle failed to deploy", result.cause());
                System.exit(1);
            }
        });
    }
}
