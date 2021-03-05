package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceModule;

public class XodusStorageDriverModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new XodusTransactionalResourceServicePersistenceModule());
        install(new XodusEnvironmentModule().withSchedulerEnvironment().withResourceEnvironment());
    }
}