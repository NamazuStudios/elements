package dev.getelements.elements.service;

import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;

/**
 * Uses the scope of the current operation to build instances of {@link NotificationBuilder}.
 */
public interface NotificationService {

    /**
     * Gets an instance of {@link NotificationBuilder}.  This includes a pre-configured {@link NotificationBuilder}
     * with the current {@link Profile} and {@link Application} therefore making it only necessary to specify the
     * recipient using {@link NotificationBuilder#recipient(Profile)}.
     *
     * @return the {@link NotificationBuilder}
     */
    NotificationBuilder getBuilder();

}
