package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.transact.JournalTransactionalResourceServicePersistenceModule;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionalPersistenceContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSChecksumAlgorithm.ADLER_32;

public class UnixFSStorageDriverModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new JournalTransactionalResourceServicePersistenceModule());
        install(new UnixFSTransactionalPersistenceContextModule().withChecksumAlgorithm(ADLER_32));
        install(new XodusEnvironmentModule().withSchedulerEnvironment());
    }
}
