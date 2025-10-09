package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.exception.InternalException;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Thread.sleep;

/**
 * Represents a transaction with the underlying datastore. This transaction object is used to acquire DAOs, perform
 * operations and then commit or rollback the transaction. This transaction assumes a snapshot and retry model where
 * transactions may fail due to transient conditions and can be retried safely. When a transaction fails due to a
 * transient condition, the {@link #commit()} method will throw a {@link RetryException}. The caller should then
 * rollback the transaction, wait for the recommended delay and then restart and re-execute the entire transaction.
 *
 * Specific implementations may provide additional guarantees, such as serializable isolation, or may not require
 * rollback and retry semantics. Consult the documentation for the specific implementation for details. In such
 * cases, the {@link RetryException} may never be thrown and code relying on it should be aware that it may not be
 * and handle that case appropriately. Typically this means that the operation will simply be executed once and
 * the exception code is completely vestigial.
 */
@ElementServiceExport
public interface Transaction extends AutoCloseable {

    /**
     * Gets the requested Dao type, bound to this {@link Transaction}. The returned DAO is only valid for the life of
     * this {@link Transaction} object.
     *
     * @param daoT   the {@link Class<DaoT>}
     * @param <DaoT> the DAO Type
     * @return the DAO Instance
     */
    <DaoT> DaoT getDao(Class<DaoT> daoT);

    /**
     * Commits the transaction. In the event the transaction fails due to a transient condition, a
     * {@link RetryException} is thrown. The caller should then rollback the transaction, wait for the recommended
     * delay and then restart and re-execute the entire transaction.
     *
     * @throws RetryException if the transaction failed due to a transient condition and can be retried safely.
     */
    void commit() throws RetryException;

    /**
     * Starts the transaction. Note, transactions are stared automatically when the Transaction object is created so
     * this is only necessary if the transaction has been explicitly aborted via rollback operation.
     */
    void start();

    /**
     * Rolls back the transaction undoing any pending changes to the database.
     */
    void rollback();

    /**
     * Closes and releases the transaction. Once closed, the transaction is no longer active and any DAOs acquired
     * will have undefined behavior if used.
     */
    void close();

    /**
     * Returns true if this transaction is still active and open.
     *
     * @return true if active, false if it has been committed or rolled back.
     */
    boolean isActive();

    /**
     * Performs the operation on this {@link Transaction}, processing the result and returning the result of the
     * operation. For transactions requiring retry semantics, this method will automatically retry the operation
     * until it succeeds, or fails with an exception other than {@link RetryException}. The transaction is also
     * closed automatically at the end of the operation regardless of success or failure.
     *
     * For implementations that do not require retry semantics, the operation is simply executed once and the
     * transaction closed. Implementations that do not require retry semantics may provide a more succinct
     * implementation of this default method.
     *
     * @param op  the operation
     * @param <T> the return type
     * @return the result of the operation
     */
    default <T> T performAndClose(final Function<Transaction, T> op) {
        try (this) {
            while (true) {

                final var result = op.apply(this);

                try {
                    commit();
                    return result;
                } catch (RetryException ex) {
                    rollback();
                    ex.waitForRecommendedDelay();
                    start();
                }

            }
        }
    }

    /**
     * Performs the operation on this {@link Transaction}, processing the result and returning the result of the
     * operation.
     *
     * @param op the operation
     */
    default void performAndCloseV(final Consumer<Transaction> op) {
        performAndClose(t -> {
            op.accept(t);
            return (Void) null;
        });
    }

    /**
     * Indicates that a transaction failed due to a transient condition and can be retried safely.
     *
     * Some implementations of {@link Transaction} use snapshot isolation, which may require client
     * code to implement retry semantics. When {@link #commit()} throws this exception, the caller
     * should restart and re-execute the entire transaction from the beginning.
     *
     * This exception contains a recommended delay in milliseconds before retrying the transaction,
     */
    class RetryException extends RuntimeException {

        public static final long DEFAULT_RECOMMENDED_DELAY = 10;

        private final long recommendDelay;

        /**
         * Default constructor with a default recommended delay of 15 milliseconds.
         */
        public RetryException() {
            this (DEFAULT_RECOMMENDED_DELAY);
        }

        /**
         * Constructor with a specified recommended delay in milliseconds.
         *
         * @param recommendDelay the recommended delay in milliseconds before retrying the transaction
         */
        public RetryException(long recommendDelay) {
            this.recommendDelay = recommendDelay;
        }

        /**
         * Gets the recommended delay in milliseconds before retrying the transaction.
         *
         * @return the recommended delay in milliseconds
         */
        public long getRecommendDelay() {
            return recommendDelay;
        }

        /**
         * Waits for the recommended delay before retrying the transaction.
         */
        public void waitForRecommendedDelay() {
            try {
                sleep(getRecommendDelay());
            } catch (InterruptedException e) {
                throw new InternalException("Interrupted waiting for recommended delay", e);
            }
        }

    }

}

