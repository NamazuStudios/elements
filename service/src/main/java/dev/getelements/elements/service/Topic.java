package dev.getelements.elements.service;

import dev.getelements.elements.Constants;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents a system-wide topic which can be used as a place to publish and subscribe to messages.  The simple
 * operations.
 *
 * Instances of {@link Topic} are considered to be lightweight and do not necessairly hold resources on their own, but
 * rather provide a means to open a connection to the underlying topic service.  Both the {@link #subscribe(Consumer)}
 * and {@link #getPublisher()} methods return types
 *
 * Created by patricktwohig on 7/20/17.
 */
public interface Topic<T> {

    Pattern VALID_NAME_PATTERN = Pattern.compile(Constants.Regexp.WHOLE_WORD_ONLY);

    /**
     * Listens for incoming messages on this {@link Topic}.
     *
     * @param tConsumer a {@link Consumer<T>} that will accept incoming messages.
     *
     * @return a {@link Runnable} which can be used to cancel the subscription.
     */
    Subscription subscribe(Consumer<T> tConsumer);

    /**
     * Similar to {@link #subscribe(Consumer)}, this will subscribe to messages.  However, this
     * will automatically invoke {@link Subscription#close()} once the first message has been
     * processed.
     *
     * One of either {@link Consumer} will be called, at most, once.
     *
     * @param consumer the {@link Consumer<T>} which will accept the next message
     * @param exceptionConsumer a {@link Consumer<Exception>} instance which will handle possible exceptions invoking {@link Subscription#close()}
     *
     * @return a {@link Subscription} instance.  Invoking {@link Subscription#close()} a second time on this is guaranteed to have no ill-effects
     */
    default Subscription subscribeNext(final Consumer<T> consumer, final Consumer<Exception> exceptionConsumer) {

        final AtomicReference<Topic.Subscription> subscriptionAtomicReference = new AtomicReference<>();

        final Consumer<T> wrapped = t -> {

            final Topic.Subscription subscription = subscriptionAtomicReference.getAndSet(null);

            try {
                if (subscription != null) {
                    subscription.close();
                    consumer.accept(t);
                }
            } catch (Exception ex) {
                exceptionConsumer.accept(ex);
            }

        };

        try {
            subscriptionAtomicReference.set(subscribe(wrapped));
        } catch (final Exception ex) {
            exceptionConsumer.accept(ex);
            return () -> {};
        }

        return () -> {

            final Topic.Subscription subscription = subscriptionAtomicReference.getAndSet(null);

            try {
                if (subscription != null) {
                    subscription.close();
                }
            } catch (Exception ex) {
                exceptionConsumer.accept(ex);
            }

        };

    }

    /**
     * Opens a connection to the underlying topic service and allows for the publication of messages
     * on a particuilar topic.
     */
    Publisher<T> getPublisher();

    /**
     * Returns a subtopic with the provided name.  The sub-topic's name will be appended
     * to this topic's name.  Events published to this topic will
     *
     * @param name
     * @return
     */
    Topic<T> getSubtopicNamed(String name);

    /**
     * Represents a subscription.  LazyValue {@link #close()} has been invoked the associated
     * {@link Consumer} will no longer receive any further calls to to its {@link Consumer#accept(Object)}
     * method.
     */
    interface Subscription extends AutoCloseable {

        /**
         * For the sake of convenience, this omits the {@link Exception} specification.
         */
        @Override
        void close();

    }

    /**
     * The Publisher type which is returned by {@link #getPublisher()}.  This will keep a connection
     * open to the underlying topic service and publish messages consumed by the {@link #accept(Object)}
     * mnethod until it is closed using {@link #close()}.
     *
     * @param <U>
     */
    interface Publisher<U> extends Consumer<U>, AutoCloseable {

        /**
         * For the sake of convenience, this omits the {@link Exception} specification.
         */
        @Override
        default void close() {};

    }

    /**
     * Checks if the supplied {@link Topic} name is valid.
     *
     * @param name the name to check
     * @return the value passed to the function
     * @throws IllegalArgumentException if the name is not valid
     */
    static String checkValidName(String name) {

        name = nullToEmpty(name).trim();

        if (!VALID_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(name + " is not a valid topic name.");
        }

        return name;

    }


}
