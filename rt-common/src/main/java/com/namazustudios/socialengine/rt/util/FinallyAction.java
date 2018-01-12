package com.namazustudios.socialengine.rt.util;

/**
 * A {@link FunctionalInterface} which performs a series of actions when it is closed.  Ideally to be used in a finally
 * block or in a try-with-resources to chain additional destruction steps.
 */
@FunctionalInterface
public interface FinallyAction extends AutoCloseable {

    /**
     * Perform the {@link FinallyAction}.
     */
    void perform();

    default FinallyAction then(final FinallyAction next) {
        return () -> {
            try {
                perform();
            } finally {
                next.perform();
            }
        };
    }

    @Override
    default void close() {
        perform();
    }

    /**
     * Convienience method to start a chain of {@link FinallyAction} instances.
     *
     * @param first the next in the chain
     * @return
     */
    static FinallyAction with(final FinallyAction first) { return first; }

}
