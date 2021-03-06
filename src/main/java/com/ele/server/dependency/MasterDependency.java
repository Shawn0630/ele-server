package com.ele.server.dependency;

import akka.actor.ActorSystem;
import com.ele.data.repositories.MockStorage;
import com.ele.data.repositories.SystemStorage;
import com.ele.data.repositories.cassandra.CassandraStorage;
import com.ele.data.repositories.file.FileStorage;
import com.google.inject.AbstractModule;
import com.typesafe.config.Config;

public class MasterDependency extends AbstractModule {

    private final Config config;
    private final static String CLUSTER_NAME_KEY = "ele.server.cluster.name";

    public MasterDependency(Config config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        install(new VertxModule());
        bind(SystemStorage.class).to(CassandraStorage.class).asEagerSingleton();
        bind(MockStorage.class).to(FileStorage.class).asEagerSingleton();

        ActorSystem system = ActorSystem.create(config.getString(CLUSTER_NAME_KEY), config);
        bind(ActorSystem.class).toInstance(system);
    }
}
