package com.namazustudios.socialengine.dao.mongo;

import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateResults;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * A class that helps reduce the boilerplate code when atomic operations are necessary.
 *
 * Created by patricktwohig on 3/29/15.
 */
@Singleton
public class MongoConcurrentUtils {

    public static final String FALLOFF_TIME_MS = "com.namazustudios.socialengine.mongo.optimistic.falloff.time.ms";

    public static final String OPTIMISTIC_RETRY_COUNT = "com.namazustudios.socialengine.mongo.optimistic.retry.count";

    @Inject
    private AdvancedDatastore datastore;

    @Inject
    @Named(OPTIMISTIC_RETRY_COUNT)
    private int numberOfRetries = 5;

    @Inject
    @Named(FALLOFF_TIME_MS)
    private int falloffTime = 100;

    /**
     * Often times it's necessary to ensure that code is only executed once in order
     * to properly satisfy an Optimistic operation.  THis provides a simple interface
     * to defince a {@link java.util.concurrent.Callable} that is only called once.
     *
     * @param once the Callable to execute once
     * @param <ReturnT> the return type
     * @return an instance of Callable which guarantees that once is only called ocne
     */
    public <ReturnT> Once<ReturnT> once(final Once<ReturnT> once) {

        return new Once<ReturnT>() {

            boolean called = false;

            ReturnT out = null;

            @Override
            public ReturnT call() {
                return  called && (called = true) ? (out = once.call()) : out;
            }

        };

    }

    /**
     * An interface to some code that is only called once.
     * @param <ReturnT>
     */
    @FunctionalInterface
    public interface Once<ReturnT> {

        /**
         * Called to get the fields of the code, or get it on the first call.
         *
         * @return the fields of the ReturnT
         * @throws OptimistcException if there was an Exception raised in the process.
         */
        ReturnT call();

    }

    /**
     * Attempts to complete the given criticalOperation and, in the event of a failure,
     * attempts to rety the criticalOperation several times until giving up.
     *
     * @param criticalOperation the criticalOperation to attempt
     * @param <ReturnT> the type to return
     * @return the return fields from the Operation
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
                    Thread.sleep(falloff += falloffTime);
                    continue;
                } catch (InterruptedException ex) {
                    throw new IllegalStateException("Interrupted while falling off.", ex);
                }
            }

        } while (attempts < numberOfRetries);

        throw new ConflictException("Exceeded number of retries " + numberOfRetries);

    }

    /**
     * Given a {@link Query<ModelT>} object, this will attempt to find a single entity and execute
     * an atomic update.
     *
     * The result of the query, or a freshly created instance, is passed to
     * {@link MongoConcurrentUtils.CriticalOperationWithModel#attempt(AdvancedDatastore, Object)}
     * just before attempting the write.
     *
     * Before attempting an update, a snapshot of the object is taken using {@link Datastore#queryByExample(Object)}
     * to ensure that the object can be updated completely (or not at all).
     *
     * In the event the operation fails, the operation is retried until either it succeeds or a timeout happens.
     *
     * @param query the query to find the objects
     * @param operation the operation to execute
     *
     * @throws IllegalArgumentException if the query does not return a single result
     * @throws ContentionException if the atomic operation fails
     *
     */
    public <ReturnT, ModelT> ReturnT performOptimisticUpsert(
            final Query<ModelT> query,
            final CriticalOperationWithModel<ReturnT, ModelT> operation) throws ConflictException {

        return performOptimistic(datastore -> {

            final ModelT model;
            final Key<ModelT> key;

            if (datastore.getCount(query) == 0) {

                key = null;

                try {
                    model = query.getEntityClass().newInstance();
                } catch (InstantiationException ex) {
                    throw new IllegalStateException(ex);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(ex);
                }

            } else if (datastore.getCount(query) == 1) {
                model = query.get();
                key = datastore.getKey(model);
            } else {
                throw new IllegalArgumentException("Multiple objects exist for query.");
            }

            final Query<ModelT> qbe = datastore.queryByExample(model);
            final ReturnT out = operation.attempt(datastore, model);

            if (key != null && !Objects.equals(key, datastore.getKey(model))) {

                // If we were looking to update a specific object we had better make sure
                // that nobody fucked with the key or else this operation will not behave
                // properly.

                throw new IllegalArgumentException("Key mismatch.  Expected " + key +
                        " but got " + datastore.getKey(model) + " instead.");

            }

            final UpdateResults result = datastore.updateFirst(qbe, model, true);

            if (!(result.getUpdatedCount() == 1 || result.getInsertedCount() == 1)) {
                throw new ContentionException();
            }

            return out;

        });
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
     * A basic a atomic operation.  The operation is supplied with a
     * {@link org.mongodb.morphia.Datastore} instance which is used to handel the atomic
     * operation.
     *
     * @param <ReturnT> the operation
     */
    public interface CriticalOperationWithModel<ReturnT, ModelT> {

        /**
         * Defines the logic for hte operation.
         *
         * @param datastore the datastore
         * @param model the model to edit
         *
         * @return the return type
         */
        ReturnT attempt(final AdvancedDatastore datastore, final ModelT model) throws ContentionException;

    }

    /**
     * General exception type for a failure of an Optimistic operation.
     */
    public class OptimistcException extends Exception {

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
     * Thrown when the operation fails.  In the event an {@link MongoConcurrentUtils.CriticalOperation}
     * fails because the object has changed, this exception may be raised to re-attempt the operation.
     */
    public class ConflictException extends OptimistcException {

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
     * Thrown when there is too much contention over a particular resource.  If an optimistic exception
     * fails too many times then this exception is raised.
     */
    public class ContentionException extends  OptimistcException {

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
