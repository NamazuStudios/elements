package com.namazustudios.socialengine.rt.transact;

import com.google.inject.PrivateModule;

public class SimpleTransactionalResourceServicePersistenceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(TransactionalResourceServicePersistence.class)
            .to(SimpleTransactionalResourceServicePersistence.class)
            .asEagerSingleton();

        expose(TransactionalResourceServicePersistence.class);

    }

}
