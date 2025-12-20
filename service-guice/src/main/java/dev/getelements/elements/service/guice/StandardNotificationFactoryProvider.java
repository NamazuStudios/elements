package dev.getelements.elements.service.guice;


import com.google.inject.Injector;
import dev.getelements.elements.sdk.service.notification.Notification;
import dev.getelements.elements.sdk.service.notification.NotificationFactory;
import dev.getelements.elements.service.notification.StandardNotification;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class StandardNotificationFactoryProvider implements Provider<NotificationFactory> {

    @Inject
    private Provider<StandardNotification> notificationProvider;

    @Override
    public NotificationFactory get() {
        return p -> {
            final StandardNotification standardNotification = notificationProvider.get();
            standardNotification.initialize(p);
            return standardNotification;
        };
    }

}
