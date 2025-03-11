package dev.getelements.elements.sdk.service.notification;

import dev.getelements.elements.sdk.model.profile.Profile;

import java.util.function.Consumer;

/**
 * Represents a single destination for {@link Notification}.  As a single {@link Profile} may have multiple
 * registration instances, this will accept the various {@link Consumer}s and {@link NotificationParameters} to send
 * to each registered destination.
 */
@FunctionalInterface
public interface NotificationDestination {

    /**
     * Sends a notification using the supplied {@link NotificationParameters} and calblack medhos.
     *
     * @param parameters
     * @param success
     * @param failure
     */
    void send(NotificationParameters parameters, Consumer<NotificationEvent> success, Consumer<Exception> failure);

}
