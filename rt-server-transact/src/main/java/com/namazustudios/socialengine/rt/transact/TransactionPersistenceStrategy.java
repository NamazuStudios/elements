package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.PersistenceStrategy;
import com.namazustudios.socialengine.rt.id.ResourceId;

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
