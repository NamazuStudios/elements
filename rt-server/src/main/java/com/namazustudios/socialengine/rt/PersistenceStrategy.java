package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a means to a persist the state of {@link Resource} instances as managed by an associated
 * {@link ResourceService}.
 */
public interface PersistenceStrategy {

    /**
     * Requests that the {@link ResourceId} with the supplied {@link ResourceId} be persisted.  Throwing an exception
     * if the operation is not possible at the time being.
     *
     * @param resourceId the {@link ResourceId} to persist
     */
    void persist(ResourceId resourceId);

    /**
     * Gets a null {@link PersistenceStrategy} which is designed to work with {@link ResourceService} types which do not
     * support persistence at all.
     *
     * @return
     */
    static PersistenceStrategy getNullPersistence() {
        return new PersistenceStrategy() {

            private final Logger logger = LoggerFactory.getLogger(PersistenceStrategy.class);

            @Override
            public void persist(final ResourceId resourceId) {
                logger.warn("Persistence not supported.  Not persisting {}", resourceId);
            }

            @Override
            public String toString() {
                return "Null PersistenceStrategy";
            }

        };
    }

}
