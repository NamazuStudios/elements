package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.id.TaskId;

import java.util.Map;
import java.util.Optional;

/**
 * Used to indicate a {@link TaskEntry<?>} which is null. This instance will always behave as if the {@link TaskEntry}
 * is not present.
 *
 * @param <ScopeT>
 */
public class NullTaskEntry<ScopeT> extends AbstractTaskEntry<ScopeT> {

    private NullTaskEntry() {
        super(new OperationalStrategy<>() {});
    }

    @Override
    public Optional<ScopeT> findOriginalScope() {
        return Optional.empty();
    }

    @Override
    public Map<TaskId, TransactionalTask> getOriginalTasksImmutable() {
        throw new UnsupportedOperationException("Not supported for null instance.");
    }

    @Override
    public void flush(final TransactionJournal.MutableEntry mutableEntry) {
        throw new UnsupportedOperationException("Not supported for null instance.");
    }

    @Override
    public void close() {}

    private static NullTaskEntry<?> singleton = new NullTaskEntry();

    public static boolean isNull(final TaskEntry<?>entry) {
        return entry == singleton;
    }

    public static <T> NullTaskEntry<T> nullInstance() {
        return (NullTaskEntry<T>) singleton;
    }

}
