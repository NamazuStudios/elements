package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.PersistenceStrategy;
import dev.getelements.elements.rt.id.ResourceId;

import javax.inject.Inject;

public class TransactionPersistenceStrategy implements PersistenceStrategy {

    private TransactionalResourceService transactionalResourceService;

    @Override
    public void persist(final ResourceId resourceId) {
        getTransactionalResourceService().persist(resourceId);
    }

    public TransactionalResourceService getTransactionalResourceService() {
        return transactionalResourceService;
    }

    @Inject
    public void setTransactionalResourceService(TransactionalResourceService transactionalResourceService) {
        this.transactionalResourceService = transactionalResourceService;
    }

}
