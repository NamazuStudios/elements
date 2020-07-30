package com.namazustudios.socialengine.rt.transact.unix;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.TransactionJournal;
import com.namazustudios.socialengine.rt.transact.TransactionalPersistenceContext;

public class UnixFSTransactionalPersistenceContextModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(UnixFSUtils.class);

        bind(TransactionalPersistenceContext.class)
            .to(UnixFSTransactionalPersistenceContext.class);

        bind(TransactionJournal.class)
            .to(UnixFSTransactionJournal.class)
            .asEagerSingleton();

        bind(UnixFSRevisionPool.class)
            .asEagerSingleton();

        bind(UnixFSRevisionTable.class)
            .asEagerSingleton();

        bind(Revision.Factory.class)
            .to(UnixFSRevisionPool.class);

        bind(UnixFSGarbageCollector.class)
            .asEagerSingleton();

        expose(TransactionJournal.class);
        expose(TransactionalPersistenceContext.class);

    }

}
