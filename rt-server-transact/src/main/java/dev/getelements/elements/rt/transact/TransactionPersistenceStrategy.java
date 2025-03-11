package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.PersistenceStrategy;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

import jakarta.inject.Inject;

public class TransactionPersistenceStrategy implements PersistenceStrategy {

    private TransactionalResourceService transactionalResourceService;

    @Override
    public void persist(final ResourceId resourceId) {
        // TODO Figure this out.
//        getTransactionalResourceService().persist(resourceId);
    }

    public TransactionalResourceService getTransactionalResourceService() {
        return transactionalResourceService;
    }

    @Inject
    public void setTransactionalResourceService(TransactionalResourceService transactionalResourceService) {
        this.transactionalResourceService = transactionalResourceService;
    }

}
