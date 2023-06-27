package dev.getelements.elements.rt.transact;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.PersistenceEnvironment;

public class JournalTransactionalResourceServicePersistenceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(JournalTransactionalResourceServicePersistenceEnvironment.class);
        bind(PersistenceEnvironment.class).to(JournalTransactionalResourceServicePersistenceEnvironment.class);
        bind(TransactionalResourceServicePersistence.class).to(JournalTransactionalResourceServicePersistenceEnvironment.class);

        expose(PersistenceEnvironment.class);
        expose(TransactionalResourceServicePersistence.class);

    }

}
