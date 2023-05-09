package dev.getelements.elements.appnode.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.xodus.XodusEnvironmentModule;
import dev.getelements.elements.rt.xodus.XodusTransactionalResourceServicePersistenceModule;

public class XodusStorageDriverModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new XodusTransactionalResourceServicePersistenceModule());
        install(new XodusEnvironmentModule().withSchedulerEnvironment().withResourceEnvironment());
    }
}