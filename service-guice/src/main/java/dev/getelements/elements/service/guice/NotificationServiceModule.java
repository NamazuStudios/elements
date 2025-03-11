package dev.getelements.elements.service.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.service.notification.NotificationService;
import dev.getelements.elements.service.notification.StandardNotificationService;

public class NotificationServiceModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(NotificationService.class).to(StandardNotificationService.class);
        expose(NotificationService.class);
    }

}
