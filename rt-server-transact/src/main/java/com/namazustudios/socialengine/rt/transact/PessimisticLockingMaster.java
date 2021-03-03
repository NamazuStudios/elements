package com.namazustudios.socialengine.rt.transact;

/**
 * Represents a master record which can be used to create instances of {@link PessimisticLocking}.
 */
public interface PessimisticLockingMaster {

    /**
     * Creates a new instance of {@link PessimisticLocking}.
     *
     * @return the {@link PessimisticLocking} instance
     */
    PessimisticLocking newPessimisticLocking();

}
