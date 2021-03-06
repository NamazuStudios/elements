package com.namazustudios.socialengine.rt.remote;

import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * Behaves similar to {@link Consumer <Throwable>} except that it may allow for re-throwing of the underlying
 * {@link Throwable}.
 */
@FunctionalInterface
public
interface InvocationErrorConsumer {

    /**
     * Accepts the {@link Throwable} and processes it.  If necessary it can re-throw it, or wrap it in another type
     * and throw that.
     * @param invocationError the {@link Throwable} instance
     * @throws Throwable if the supplied
     */
    void accept(InvocationError invocationError);

    /**
     * Invokes {@link #accept(InvocationError)}, catching any {@link Throwable} instances and logging them to the
     * supplied instance of {@link Logger}.
     *
     * @param logger the logger to accep the {@link Throwable}
     */
    default void acceptAndLogError(final Logger logger, final InvocationError invocationError) {
        try {
            accept(invocationError);
        } catch (Exception ex) {
            logger.error("Caught throwable handling error.", ex);
        }
    }

}
