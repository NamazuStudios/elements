package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.rt.exception.InternalException;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateResults;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

import static java.lang.Thread.sleep;

/**
 * A class that helps reduce the boilerplate code when atomic operations are necessary.
 *
 * Created by patricktwohig on 3/29/15.
 */
@Singleton
public class MongoConcurrentUtils {

    public static final String OPTIMISTIC_RETRY_COUNT = "com.namazustudios.socialengine.mongo.optimistic.retry.count";

    /**
     * Defines the minimum time an optimistic operation will wait before retrying.
     */
    public static final String FALLOFF_TIME_MIN_MS = "com.namazustudios.socialengine.mongo.optimistic.falloff.time.min.msec";

    /**
     * Defines the maximum time an optimistic operation will wait before retrying.
     */
    public static final String FALLOFF_TIME_MAX_MS = "com.namazustudios.socialengine.mongo.optimistic.falloff.time.max.msec";

    @Inject
    private AdvancedDatastore datastore;

    @Inject
    @Named(OPTIMISTIC_RETRY_COUNT)
    private int numberOfRetries = 5;

    @Inject
    @Named(FALLOFF_TIME_MIN_MS)
    private int falloffTimeMin;

    @Inject
    @Named(FALLOFF_TIME_MAX_MS)
    private int falloffTimeMax;

    /**
     * Attempts to complete the given criticalOperation and, in the event of a failure, attempts to retry the
     * criticalOperation several times until giving up.
     *
     * @param criticalOperation the criticalOperation to attempt
     * @param <ReturnT> the type to return
     * @return the return fields from the HttpOperation
     *
     */
    public <ReturnT> ReturnT performOptimistic(final CriticalOperation<ReturnT> criticalOperation) throws ConflictException {

        int attempts = 0;
        int falloff = 0;

        do {

            ++attempts;

            try {
                return criticalOperation.attempt(datastore);
            } catch (ContentionException e) {
                try {
                    // Random spread helps ensure that we don't get repeat contention among the operations.
                    final Random random = ThreadLocalRandom.current();
                    final int sleepTime = falloffTimeMin + random.nextInt(falloffTimeMax - falloffTimeMin);
                    sleep(falloff += sleepTime);
                    continue;
                } catch (InterruptedException ex) {
                    throw new InternalException("Interrupted while falling off.", ex);
                }
            }

        } while (attempts < numberOfRetries);

        throw new ConflictException("Exceeded number of retries " + numberOfRetries);

    }

    /**
     * A basic a atomic operation.  The operation is supplied with a
     * {@link org.mongodb.morphia.Datastore} instance which is used to handel the atomic
     * operation.
     *
     * @param <ReturnT> the operation
     */
    @FunctionalInterface
    public interface CriticalOperation<ReturnT> {

        /**
         * Defines the logic for hte operation.
         * @param datastore the datastore
         * @return the return type
         */
        ReturnT attempt(final AdvancedDatastore datastore) throws ContentionException;

    }

    /**
     * General exception type for a failure of an Optimistic operation.
     */
    public static class OptimistcException extends Exception {

        public OptimistcException() {}

        public OptimistcException(String message) {
            super(message);
        }

        public OptimistcException(String message, Throwable cause) {
            super(message, cause);
        }

        public OptimistcException(Throwable cause) {
            super(cause);
        }

        public OptimistcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

    }


    /**
     * Thrown when there is too much contention over a particular resource.  If an optimistic exception
     * fails too many times then this exception is raised.
     */
    public static class ConflictException extends OptimistcException {

        public ConflictException() {}

        public ConflictException(String message) {
            super(message);
        }

        public ConflictException(String message, Throwable cause) {
            super(message, cause);
        }

        public ConflictException(Throwable cause) {
            super(cause);
        }

        public ConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    /**
     * Thrown when the operation fails.  In the event an {@link MongoConcurrentUtils.CriticalOperation}
     * fails because the object has changed, this exception may be raised to re-attempt the operation.
     */
    public static class ContentionException extends  OptimistcException {

        public ContentionException() {}

        public ContentionException(String message) {
            super(message);
        }

        public ContentionException(String message, Throwable cause) {
            super(message, cause);
        }

        public ContentionException(Throwable cause) {
            super(cause);
        }

        public ContentionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

    }

}
