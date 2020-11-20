package com.namazustudios.socialengine.rt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link FunctionalInterface} which performs a series of actions when it is closed.  Ideally to be used in a finally
 * block or in a try-with-resources to chain additional destruction steps.
 */
@FunctionalInterface
public interface FinallyAction extends Runnable, AutoCloseable {

    FinallyAction BEGIN = () -> {};

    Logger logger = LoggerFactory.getLogger(FinallyAction.class);

    /**
     * Begins the finally action.
     *
     * @return
     */
    static FinallyAction begin() {
        return BEGIN;
    }

    /**
     * Begins the finally action.
     *
     * @return
     */
    static FinallyAction begin(final Logger logger) {
        return new FinallyAction() {
            @Override
            public void run() {}

            @Override
            public Logger getLogger() { return logger; }
        };
    }

    /**
     * Gets a {@link Logger} used to log exceptions thrown while processing the {@link FinallyAction}. The default
     * returns {@link FinallyAction#logger}.
     *
     * @return the {@link Logger}
     */
    default Logger getLogger() {
        return logger;
    }

    /**
     * Chains this {@link FinallyAction} to the next {@link FinallyAction}.
     *
     * @param next
     * @return
     */
    default FinallyAction then(final Runnable next) {
        return new FinallyAction() {

            @Override
            public void run() {
                try {
                    FinallyAction.this.run();
                } catch (final Exception ex) {
                    getLogger().error("Caught exception.", ex);
                    throw ex;
                } finally {
                    next.run();
                }
            }

            @Override
            public Logger getLogger() {
                return FinallyAction.this.getLogger();
            }

        };
    }

    @Override
    default void close() {
        run();
    }

    /**
     * Convenience method to start a chain of {@link FinallyAction} instances.
     *
     * @param first the next in the chain
     * @return a new {@link FinallyAction}
     */
    static FinallyAction with(final FinallyAction first) { return first; }

}
