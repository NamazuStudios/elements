package com.namazustudios.socialengine.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Runtime.getRuntime;

/**
 * Created by patricktwohig on 8/23/17.
 */
public class ShutdownHooks {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHooks.class);

    private final AtomicReference<Action> actions;

    public ShutdownHooks(final Class<?> aClass) {
        actions = new AtomicReference<>(() -> logger.info("Running cleanup hooks up for {}", aClass.getName()));
        final Thread thread = new Thread(() -> actions.get().perform());
        getRuntime().addShutdownHook(thread);
    }

    public void add(final Object context, final Action action) {
        actions.getAndUpdate(a -> a.andThen(context, action));
    }

    @FunctionalInterface
    public interface Action {

        void perform();

        default Action andThen(final Object context, final Action next) {

            final Action previous = this;

            return () -> {

                try {
                    previous.perform();
                } catch (Exception ex) {
                    logger.error("Caught cleanup exception.", ex);
                }

                logger.info("Cleaning up {}", context);
                next.perform();
                logger.info("Cleaned up {}", context);

            };
        }

    }

}
