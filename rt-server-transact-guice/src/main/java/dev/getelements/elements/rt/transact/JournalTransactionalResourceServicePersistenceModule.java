package dev.getelements.elements.rt.transact;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.LockSetService;
import dev.getelements.elements.rt.PersistenceEnvironment;
import dev.getelements.elements.rt.WeakReferenceLockSetService;

public class JournalTransactionalResourceServicePersistenceModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(Snapshot.Builder.class).to(StandardSnapshotBuilder.class);
        bind(LockSetService.class).to(WeakReferenceLockSetService.class).asEagerSingleton();
        bind(JournalTransactionalResourceServicePersistenceEnvironment.class).asEagerSingleton();
        bind(PersistenceEnvironment.class).to(JournalTransactionalResourceServicePersistenceEnvironment.class);
        bind(TransactionalResourceServicePersistence.class).to(JournalTransactionalResourceServicePersistenceEnvironment.class);

        expose(PersistenceEnvironment.class);
        expose(TransactionalResourceServicePersistence.class);

    }

}
