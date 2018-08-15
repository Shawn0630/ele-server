package com.ele.server.dependency;

import com.ele.data.repositories.MySQLStorage;
import com.ele.data.repositories.SystemStorage;
import com.google.inject.AbstractModule;

public class MasterDependency extends AbstractModule {
    @Override
    protected void configure() {
        bind(SystemStorage.class).to(MySQLStorage.class).asEagerSingleton();
    }
}
