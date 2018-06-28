package com.namazustudios.socialengine.rt;


import com.namazustudios.socialengine.rt.exception.DuplicateException;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Manages {@link Lock} instances for {@link Resource} instances.  Generally before manipulating a {@link Resource},
 * either direcly or through its {@link ResourceService} methods, it is necessary to acquire a {@link Lock} against
 * the {@link Resource}.  Note that this exists because a {@link Lock} may exist
 *
 * Created by patricktwohig on 4/11/17.
 */
public interface ResourceLockService {

    /**
     * Returns a {@link Monitor} for the provided {@link ResourceId}.
     *
     * @param resourceId the resource ID
     *
     * @return the {@link Monitor}
     */
    Monitor getMonitor(final ResourceId resourceId);

    /**
     * Deletes the lock with the given {@link ResourceId}.
     */
    void delete(ResourceId resourceId);

    /**
     * Convienience wrapper to automatically close a {@link Lock} managed by this instance.
     */
    interface Monitor extends AutoCloseable {

        /**
         * Releases the underlying {@link Lock}
         */
        @Override
        void close();

        /**
         * Gets a {@link Condition} with an arbitrary name associated with the supplied {@link Monitor}.
         *
         * @param name the name of the {@link Condition}
         * @return the {@link Condition}
         */
        default Condition getCondition(final String name) {return null;}

    }

}
