package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Uses the scope of the current operation to build instances of {@link NotificationBuilder}.
 */
public interface NotificationService {

    /**
     * Gets an instance of {@link NotificationBuilder}.  This includes a pre-configured {@link NotificationBuilder}
     * with the current {@link Profile} and {@link Application} therefore making it only necesary to specify the
     * recipient using {@link NotificationBuilder#withRecipient(Profile)}.
     *
     * @return the {@link NotificationBuilder}
     */
    NotificationBuilder getBuilder();

}
