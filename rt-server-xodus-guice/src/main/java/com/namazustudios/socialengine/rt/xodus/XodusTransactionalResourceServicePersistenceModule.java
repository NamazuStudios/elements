package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.PersistenceEnvironment;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.transact.SimplePessimisticLockingMaster;
import com.namazustudios.socialengine.rt.transact.TransactionalResourceServicePersistence;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.DEFAULT_RESOURCE_BLOCK_SIZE;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.RESOURCE_BLOCK_SIZE;

public class XodusTransactionalResourceServicePersistenceModule extends PrivateModule {

    private Runnable bindBlockSize = () -> {};

    @Override
    protected void configure() {

        bindBlockSize.run();

        bind(PersistenceEnvironment.class).to(XodusTransactionalResourceServicePersistenceEnvironment.class);
        bind(TransactionalResourceServicePersistence.class).to(XodusTransactionalResourceServicePersistenceEnvironment.class);
        bind(XodusTransactionalResourceServicePersistenceEnvironment.class).asEagerSingleton();

        expose(PersistenceEnvironment.class);
        expose(TransactionalResourceServicePersistence.class);

    }

    /**
     * Binds the block size when storing {@link Resource}s.
     *
     * @param blockSize the block size
     * @return this instance
     */
    public XodusTransactionalResourceServicePersistenceModule withBlockSize(final long blockSize) {
        bindBlockSize = () -> bind(long.class)
            .annotatedWith(named(RESOURCE_BLOCK_SIZE))
            .toInstance(blockSize);
        return this;
    }

    /**
     * Binds the default block size when storing {@link Resource}s.
     *
     * {@link XodusTransactionalResourceServicePersistenceEnvironment#DEFAULT_RESOURCE_BLOCK_SIZE}
     *
     * @return this instance
     */
    public XodusTransactionalResourceServicePersistenceModule withDefaultBlockSize() {
        return withBlockSize(DEFAULT_RESOURCE_BLOCK_SIZE);
    }

}
