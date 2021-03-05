package com.namazustudios.socialengine.rt.transact;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.Persistence;

public class JournalTransactionalResourceServicePersistenceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(JournalTransactionalResourceServicePersistence.class);
        bind(Persistence.class).to(JournalTransactionalResourceServicePersistence.class);
        bind(TransactionalResourceServicePersistence.class).to(JournalTransactionalResourceServicePersistence.class);

        expose(Persistence.class);
        expose(TransactionalResourceServicePersistence.class);

    }

}
