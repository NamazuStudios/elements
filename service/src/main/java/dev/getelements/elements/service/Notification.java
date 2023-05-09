package dev.getelements.elements.service;

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
    default int send() {
        final Logger logger = LoggerFactory.getLogger(getClass());
        return send(ev -> logger.info("{} successfully sent.", ev), ex -> logger.error("Failed to send.", ex));
    }

    /**
     * Enqueues this {@link Notification} to be sent.  This call may block, but should return as reasonably quickly
     * as possible.  This does not guarantee delivery, and may drop the message without reporting an error if
     * necessary but should make all reasonable efforts to ensure the message has been sent.
     *
     * For example, this may ensure that the message was received by the underlying message service but will not
     * provide a guarantee that it was delivered when it returns.
     *
     * Not that either lambda may be called multiple times, or never called.
     *
     * @param success called when a message was successfully sent, note this may be called multiple times
     * @param failure called when a message failed
     *
     * @return the number of destinations that recieved the {@link Notification}
     */
    int send(Consumer<NotificationEvent> success, Consumer<Exception> failure);

}
