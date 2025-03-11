package dev.getelements.elements.sdk.service.notification;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Uses the scope of the current operation to build instances of {@link NotificationBuilder}.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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
