package dev.getelements.elements.service.notification;

import dev.getelements.elements.service.Notification;
import dev.getelements.elements.service.NotificationParameters;

import java.util.function.Function;

public interface NotificationFactory extends Function<NotificationParameters, Notification> {}
