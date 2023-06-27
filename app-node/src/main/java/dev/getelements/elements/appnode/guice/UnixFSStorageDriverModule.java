package dev.getelements.elements.appnode.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceModule;
import dev.getelements.elements.rt.transact.unix.UnixFSTransactionalPersistenceContextModule;
import dev.getelements.elements.rt.xodus.XodusEnvironmentModule;

import static dev.getelements.elements.rt.transact.unix.UnixFSChecksumAlgorithm.ADLER_32;

public class UnixFSStorageDriverModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new JournalTransactionalResourceServicePersistenceModule());
        install(new UnixFSTransactionalPersistenceContextModule().withChecksumAlgorithm(ADLER_32));
        install(new XodusEnvironmentModule().withSchedulerEnvironment());
    }
}
