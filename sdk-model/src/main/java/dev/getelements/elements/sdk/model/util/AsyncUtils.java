package dev.getelements.elements.sdk.model.util;

import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Special type for handling some async operations.
 */
public class AsyncUtils {

    private final Logger logger;

    private static final AsyncUtils defaultInstance = new AsyncUtils(getLogger(AsyncUtils.class));

    public static AsyncUtils defaultInstance() {
        return defaultInstance;
    }

    public AsyncUtils(final Class<?> cls) {
        this(getLogger(cls));
    }

    public AsyncUtils(final Logger logger) {
        this.logger = logger;
    }

    public void doNoThrowV(final Consumer<Throwable> exceptionConsumer,
                           final AsyncOperationV operation) {
        doNoThrow(exceptionConsumer, () -> {
            operation.perform();
            return null;
        }, null);
    }

    public <T> T doNoThrow(final Consumer<Throwable> exceptionConsumer,
                           final AsyncOperation<T> operation) {
        return doNoThrow(exceptionConsumer, operation, null);
    }

    public <T> T doNoThrow(final Consumer<Throwable> exceptionConsumer,
                           final AsyncOperation<T> operation,
                           final Supplier<T> ifException) {
        try {
            return operation.perform();
        } catch (Throwable ex) {
            logger.error("Async operation exception.", ex);
            exceptionConsumer.accept(ex);
            return ifException == null ? null : ifException.get();
        }
    }

    @FunctionalInterface
    public interface AsyncOperation<T> {

        T perform() throws Throwable;

    }

    @FunctionalInterface
    public interface AsyncOperationV {

        void perform() throws Throwable;

    }

}
