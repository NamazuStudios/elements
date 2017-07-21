package com.namazustudios.socialengine.service;

import java.util.function.Consumer;

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

    /**
     * Listents for incoming messages on this {@link Topic}
     *
     * @param tConsumer a {@link Consumer<T>} that will accept incoming messages.
     *
     * @return a {@link Runnable} which can be used to cancel the subscription.
     */
    Subscription subscribe(Consumer<T> tConsumer);

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
     * Represents a subscription.  Can be closed later, and once closed will no loner
     * drive calls to the associated consumer.
     */
    interface Subscription extends AutoCloseable {

        /**
         * For convienience this removes the exception spec from the super interface, but
         * expect this will throw any number of the common SocialEngine exceptions.
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
         * For convienience this removes the exception spec from the super interface, but
         * expect this will throw any number of the common SocialEngine exceptions.
         */
        @Override
        void close();

    }

}
