package dev.getelements.elements.rt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Runtime.getRuntime;

/**
 * A simple way to manage shutdown hooks. Each instance of {@link ShutdownHooks} submits a thread to the system runtime
 * which will be executed upon shutdown of the virtual machine. Each action in this instance is executed serially in
 * the order in which it was queued to this instance of {@link ShutdownHooks}.
 *
 * It is safe to queue multiple {@link ShutdownHooks.Action}s from multiple threads. This type internally uses a
 * simple lockless linked-list to queue all actions. Once queued, the action may not be removed from the queue so the
 * author of the action must ensure that there are no side effects if the associated action is no longer needed.
 *
 * Each action may throw an exception, in which case it will be logged to a logger specified by the class that owns
 * the shutdown hooks.
 *
 * Created by patricktwohig on 8/23/17.
 */
public class ShutdownHooks {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHooks.class);

    private final AtomicReference<Action> actions;

    /**
     * Uses the supplied {@link Class<?>}'s logger to log the shutdown hooks action.
     *
     * @param aClass the class which owns the {@link ShutdownHooks}
     */
    public ShutdownHooks(final Class<?> aClass) {
        this(LoggerFactory.getLogger(aClass));
    }

    /**
     * Allows for the owner of this {@link ShutdownHooks} instance to specify a custom {@link Logger} which will be used
     * to log the information of the associated actions.
     *
     * @param logger the actions
     */
    public ShutdownHooks(final Logger logger) {

        actions = new AtomicReference<>(() -> new Action() {

            @Override
            public void perform() {
                logger.info("Running shutdown hooks.");
            }

            @Override
            public Logger getLogger() {
                return logger;
            }

        });

        final Thread thread = new Thread(() -> actions.get().performNoThrow());
        thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
        getRuntime().addShutdownHook(thread);

    }

    /**
     * Adds a new {@link Action} to this {@link ShutdownHooks} instance.
     *
     * @param action the action to perform.
     */
    public void add(final Action action) {
        actions.getAndUpdate(a -> a.andThen(action));
    }

    /**
     * Adds a new {@link Action} to this {@link ShutdownHooks} instance.
     *
     * @param context the context, which will simply be logged as the {@link ShutdownHooks} performs its actions
     * @param action the action to perform.
     */
    public void add(final Object context, final Action action) {
        actions.getAndUpdate(a -> a.andThen(context, action));
    }

    @FunctionalInterface
    public interface Action {

        /**
         * Performs the action associated with the shutdown hook.
         *
         * @throws Exception any exception will be logged and the next action performed.
         */
        void perform() throws Exception;

        /**
         * Performs the action without throwing. The default implementation merely logs the exception.
         */
        default void performNoThrow() {
            try {
                perform();
            } catch (Exception ex) {
                getLogger().error("Caught exception running shutdown hook.", ex);
            }
        }

        /**
         * Generates a new {@link Action} from the the supplied action. The resulting {@link Action} will first execute
         * this instance followed immediately by the next action.
         *
         * @param next the next action to perform.
         * @return
         */
        default Action andThen(final Action next) {

            final Action previous = this;

            return () -> {
                try {
                    previous.performNoThrow();
                } finally {
                    next.perform();
                }
            };
        }

        /**
         * Identical to {@link #andThen(Action)}, but allows for some additional logging and associated context.
         *
         * @param context the context, which will be logged using {@link Object#toString()}
         * @param next the next action to perform
         * @return a new instance
         */
        default Action andThen(final Object context, final Action next) {

            final Action previous = this;

            return () -> {

                try {
                    previous.performNoThrow();
                } finally {
                    previous.getLogger().info("Cleaning up {}", context);
                    next.perform();
                    previous.getLogger().info("Cleaned up {}", context);
                }

            };
        }

        /**
         * Gets the {@link Logger} which to use when logging the shutdown hook's actions. By default this gets the
         * logger associated with {@link ShutdownHooks}.
         *
         * @return the logger instance
         */
        default Logger getLogger() {
            return logger;
        }

    }

}
