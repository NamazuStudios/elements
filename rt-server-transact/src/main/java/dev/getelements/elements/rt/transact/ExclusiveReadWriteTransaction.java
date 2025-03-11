package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.Resource;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Operates with an exclusive lock to the database. Used for bulk operations such as truncating or deleting all
 * {@link Resource}s
 */
public interface ExclusiveReadWriteTransaction extends AutoCloseable {

    void performOperation(Consumer<DataStore> operation);

    <T> T computeOperation(Function<DataStore, T> operation);

    /**
     * Closes this {@link ExclusiveReadWriteTransaction}.
     */
    void close();

}
