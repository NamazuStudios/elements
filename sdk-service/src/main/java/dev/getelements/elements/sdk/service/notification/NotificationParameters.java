package dev.getelements.elements.sdk.service.notification;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.annotation.ElementPublic;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Specifies the parameters used to send the {@link Notification}.
 */
@ElementPublic
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

    /**
     * Gets a read-only mapping of extra key/value properties associated with this notification.  If there are no
     * extra properties, an empty map is returned
     *
     * @return A read-only Map of extra properties
     */
    @Nonnull
    Map<String,String> getExtraProperties();
}