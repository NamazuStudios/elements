package dev.getelements.elements.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Represents an abstract {@link Publisher} instance.
 *
 * @param <T> the type published
 */
public abstract class AbstractPublisher<T> implements Publisher<T> {

    protected final Logger logger;

    public AbstractPublisher(final Class<?> cls) {
        this(LoggerFactory.getLogger(cls));
    }

    public AbstractPublisher(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void publish(final T t) {
        publish(t, _t -> {});
    }

    @Override
    public void publish(final T t, final Consumer<? super T> onFinish) {
        publish(t, onFinish, this::logException);
    }

    protected void logException(final Throwable th) {
        logger.error("Unexpected exception dispatching consumer.", th);
    }

    protected void handleException(final Consumer<Throwable> onException, final Throwable th) {
        try {
            onException.accept(th);
        } catch (Exception ex) {
            logException(ex);
        }
    }

}
