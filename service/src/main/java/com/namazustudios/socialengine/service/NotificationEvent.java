package com.namazustudios.socialengine.service;

import java.util.function.Consumer;

/**
 * Represents a notification event.  This is supplied to the success function of {@link #send(Consumer, Consumer)}
 * when a notification is successfully sent.
 */
public interface NotificationEvent {

    /**
     * The {@link NotificationParameters} used to send the event.
     *
     * @return
     */
    NotificationParameters getParameters();

}
