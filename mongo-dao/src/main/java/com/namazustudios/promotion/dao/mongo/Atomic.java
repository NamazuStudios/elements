package com.namazustudios.promotion.dao.mongo;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateResults;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 *
 * A class that helps reduce the boilerplate code when atomic operations are necessary.
 *
 * Created by patricktwohig on 3/29/15.
 */
@Singleton
public class Atomic {

    public static final String FALLOFF_TIME_MS = "com.namazustudios.promotion.mongo.optimistic.falloff.time.ms";

    public static final String OPTIMISTIC_RETRY_COUNT = "com.namazustudios.promotion.mongo.optimistic.retry.count";

    @Inject
    private Datastore datastore;

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
            public ReturnT call() throws OptimistcException {
                return  called && (called = true) ? (out = once.call()) : out;
            }

        };

    }

    /**
     * An interface to some code that is only called once.
     * @param <ReturnT>
     */
    public interface Once<ReturnT> {

        /**
         * Called to get the value of the code, or get it on the first call.
         *
         * @return the value of the ReturnT
         * @throws OptimistcException if there was an Exception raised in the process.
         */
        ReturnT call() throws OptimistcException;

    }

    /**
     * Attempts to complete the given criticalOperation and, in the event of a failure,
     * attempts to rety the criticalOperation several times until giving up.
     *
     * @param criticalOperation the criticalOperation to attempt
     * @param <ReturnT> the type to return
     * @return the return value from the Operation
     *
     */
    public <ReturnT> ReturnT performOptimistic(final CriticalOperation<ReturnT> criticalOperation) throws OptimistcException {

        int retries = 0;
        int falloff = 0;

        do {

            try {
                return criticalOperation.attempt(datastore);
            } catch (ConflictException e) {
                try {
                    Thread.sleep(falloff += falloffTime);
                    continue;
                } catch (InterruptedException ex) {
                    throw new OptimistcException("Interrupted while falling off.", ex);
                }
            }

        } while (retries < numberOfRetries);

        throw new ContentionException("Exceeded number of retries " + numberOfRetries);

    }

    /**
     * Given a model object, this performs the given {@link Atomic.CriticalOperation} taking a Query By Example using
     * {@link Datastore#queryByExample(Object)} just before the execution of the
     * {@link Atomic.CriticalOperation#attempt(org.mongodb.morphia.Datastore)} method to ensure the model is properly
     * refreshed.
     *
     * After the operation is attempted, an attempt to insert using the generated Query By Example to ensure that
     * the entire object is successfully udpated or not at all.  Similar to a compare and swap operation.
     *
     * @param modelToEdit the model object to edit
     * @param operation the edit operation to perfrom
     * @param <ReturnT> the return Type
     * @param <ModelT> the model type
     * @return the result of the given operation
     * @throws OptimistcException if an exception occurred updaing the object.
     */
    public <ReturnT, ModelT> ReturnT performOptimisticUpsert(
            final ModelT modelToEdit,
            final CriticalOperation<ReturnT> operation) throws OptimistcException {

        return performOptimistic(new CriticalOperation<ReturnT>() {

            @Override
            public ReturnT attempt(Datastore datastore) throws OptimistcException {

                final Key<?> key = datastore.getKey(modelToEdit);

                if (key.getId() != null) {
                    datastore.get(modelToEdit);
                }

                final Query<ModelT> qbe = datastore.queryByExample(modelToEdit);
                final ReturnT out = operation.attempt(datastore);

                final UpdateResults result = datastore.updateFirst(qbe, modelToEdit, true);

                if (!(result.getUpdatedCount() == 1 || result.getInsertedCount() == 1)) {
                    throw new ConflictException();
                }

                return out;

            }

        });
    }

    /**
     * A basic a atomic operation.  The operation is supplied with a
     * {@link org.mongodb.morphia.Datastore} instance which is used to handel the atomic
     * operation.
     *
     * @param <ReturnT> the operation
     */
    public interface CriticalOperation<ReturnT> {

        /**
         * Defines the logic for hte operation.
         * @param datastore
         * @return the return type
         */
        ReturnT attempt(final Datastore datastore) throws OptimistcException;

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
     * Thrown when the operation fails.  In the event an {@link com.namazustudios.promotion.dao.mongo.Atomic.CriticalOperation}
     * fails because the object has changed, this exception may be raised to re-attempt the operation.
     */
    public class ConflictException extends OptimistcException {

        public ConflictException() {
        }

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
