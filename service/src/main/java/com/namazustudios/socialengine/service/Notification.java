package com.namazustudios.socialengine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Once configured, this will perform the action to finally send the {@link Notification}.  Like instances of
 * {@link NotificationBuilder}, instances will be short-lived and are not guaranteed to be thread-safe.  Once a
 * {@link Notification} has been sent, the object should be disposed of.
 */
public interface Notification {

    /**
     * Invokes {@link #send(Consumer, Consumer)} with callbacks which simply logs the result.
     */
    default void send() {
        final Logger logger = LoggerFactory.getLogger(getClass());
        send(v -> logger.info("{} successfully sent."), ex -> logger.error("Failed to send.", ex));
    }

    /**
     * Enqueues this {@link Notification} to be sent.  This call may block, but should return as reasonably quickly
     * as possible.  This does not guarantee delivery, and may drop the message without reporting an error if
     * necessary but should make all reasonable efforts to ensure the message has been sent.
     *
     * For example, this may ensure that the message was received by the underlying message service but will not
     * provide a guarantee that it was delivered when it returns.
     *
     * @param success called when a message was successfully sent
     * @param failure called when a message failed
     */
    void send(Consumer<Void> success, Consumer<Exception> failure);

}
