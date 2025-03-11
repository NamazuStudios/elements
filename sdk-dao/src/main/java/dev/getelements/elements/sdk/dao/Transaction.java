package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a transaction with
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
     * Commits the transaction.
     */
    void commit();

    /**
     * Rolls back the transaction.
     */
    void rollback();

    /**
     * Closes and releases the transaction.
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
     * operation.
     *
     * @param op  the operation
     * @param <T> the return type
     * @return the result of the operation
     */
    default <T> T performAndClose(final Function<Transaction, T> op) {
        try (this) {
            final var result = op.apply(this);
            commit();
            return result;
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

}
