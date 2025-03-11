package dev.getelements.elements.sdk.service.notification;

import dev.getelements.elements.sdk.annotation.ElementPublic;

import java.util.function.Function;

/**
 * A Factory type which returns a {@link NotificationFactory}.
 */
@ElementPublic
public interface NotificationFactory extends Function<NotificationParameters, Notification> {}
