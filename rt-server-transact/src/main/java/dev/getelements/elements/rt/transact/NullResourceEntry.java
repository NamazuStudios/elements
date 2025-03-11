package dev.getelements.elements.rt.transact;

import java.util.Optional;

public class NullResourceEntry extends AbstractResourceEntry {

    private NullResourceEntry() {
        super(new OperationalStrategy() {});
    }

    @Override
    public Optional<ResourceContents> findOriginalResourceContents() {
        return Optional.empty();
    }

    @Override
    public void flush(final TransactionJournal.MutableEntry mutableEntry) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString() {
        return "<null entry>";
    }

    private static final NullResourceEntry singleton = new NullResourceEntry();

    /**
     * Gets the null instance.
     *
     * @return the null instance
     */
    public static NullResourceEntry nullInstance() {
        return singleton;
    }

    public static boolean isNull(final ResourceEntry entry) {
        return entry == singleton;
    }

}
