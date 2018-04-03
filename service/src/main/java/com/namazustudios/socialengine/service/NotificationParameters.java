package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Specifies the parameters used to send the {@link Notification}.
 */
public interface NotificationParameters {

    /**
     * Gets the singular {@link Profile} who will recieve the notification.
     * @return
     */
    Profile getRecipient();

    /**
     * Gets the {@link Application} sending the notification.
     *
     * @return the {@link Application}
     */
    Application getApplication();

    /**
     * Ges the title of the notification.
     *
     * @return the title
     */
    String getTitle();

    /**
     * Gets the message of the notification.
     *
     * @return the message of the notification
     */
    String getMessage();

    /**
     * Gets the sound to play when delivering the message.  Null indicates no sound will be played.
     *
     * @return the sound name or null
     */
    String getSound();

}
