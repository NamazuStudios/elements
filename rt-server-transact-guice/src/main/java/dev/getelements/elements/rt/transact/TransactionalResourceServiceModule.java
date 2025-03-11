package dev.getelements.elements.rt.transact;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.PersistenceStrategy;
import dev.getelements.elements.rt.ResourceService;

public class TransactionalResourceServiceModule extends PrivateModule {

    private Runnable exposeTransactionalResourceServiceAction = () -> {};

    @Override
    protected void configure() {

        bind(TransactionalResourceService.class).asEagerSingleton();
        bind(TransactionPersistenceStrategy.class).asEagerSingleton();

        bind(ResourceService.class).to(TransactionalResourceService.class);
        bind(PersistenceStrategy.class).to(TransactionPersistenceStrategy.class);

        expose(ResourceService.class);
        expose(PersistenceStrategy.class);

        exposeTransactionalResourceServiceAction.run();

    }

    /**
     * Tells this {@link TransactionalResourceServiceModule} to expose the underlying
     * {@link TransactionalResourceService}. This is useful for testing and should not be used in production code.
     *
     * @return this instance
     */
    public TransactionalResourceServiceModule exposeTransactionalResourceService() {
        exposeTransactionalResourceServiceAction = () -> expose(TransactionalResourceService.class);
        return this;
    }

}
