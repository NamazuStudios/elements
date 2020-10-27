package com.namazustudios.socialengine.rt.transact;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.Persistence;

public class SimpleTransactionalResourceServicePersistenceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(SimpleTransactionalResourceServicePersistence.class);
        bind(Persistence.class).to(SimpleTransactionalResourceServicePersistence.class);
        bind(TransactionalResourceServicePersistence.class).to(SimpleTransactionalResourceServicePersistence.class);

        expose(Persistence.class);
        expose(TransactionalResourceServicePersistence.class);

    }

}
