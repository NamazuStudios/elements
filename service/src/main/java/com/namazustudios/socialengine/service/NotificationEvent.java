package com.namazustudios.socialengine.service;

import java.util.function.Consumer;

/**
 * Represents a notification event.  This is supplied to the success function of
 * {@link Notification#send(Consumer, Consumer)} when a notification is successfully sent.
 */
public interface NotificationEvent {

    /**
     * Returns the database-assigned token ID for the {@link NotificationEvent}.
     * @return
     */
    String getTokenId();

    /**
     * The {@link NotificationParameters} used to send the event.
     *
     * @return
     */
    NotificationParameters getParameters();

}
