package com.ele.server.dependency;

import com.ele.data.repositories.MySQLStorage;
import com.google.inject.AbstractModule;

public class MasterDependency extends AbstractModule {
    @Override
    protected void configure() {
        bind(MySQLStorage.class).asEagerSingleton();
    }
}
