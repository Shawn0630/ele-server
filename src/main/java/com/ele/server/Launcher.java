package com.ele.server;

import com.ele.data.repositories.MySQLStorage;
import com.ele.server.dependency.MasterDependency;
import com.github.racc.tscg.TypesafeConfigModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.vertx.core.Vertx;

public class Launcher {

    public static void main(String[] args) {
        Injector injector;
        Config config = ConfigFactory.defaultApplication();
        config = config.withFallback(ConfigFactory.defaultReference());
        injector = Guice.createInjector(TypesafeConfigModule.fromConfigWithPackage(config, "com.ele"), new MasterDependency());

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ServerVerticle());
    }
}