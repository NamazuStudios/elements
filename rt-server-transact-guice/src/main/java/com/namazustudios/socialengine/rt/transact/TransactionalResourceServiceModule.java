package com.namazustudios.socialengine.rt.transact;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.PersistenceStrategy;
import com.namazustudios.socialengine.rt.ResourceService;

public class TransactionalResourceServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(ResourceService.class)
            .to(TransactionalResourceService.class)
            .asEagerSingleton();

        bind(PersistenceStrategy.class)
            .to(TransactionPersistenceStrategy.class);

        expose(ResourceService.class);
        expose(PersistenceStrategy.class);

    }

}
