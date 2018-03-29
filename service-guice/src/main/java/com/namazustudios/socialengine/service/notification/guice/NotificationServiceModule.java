package com.namazustudios.socialengine.service.notification.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.service.NotificationService;
import com.namazustudios.socialengine.service.notification.StandardNotificationService;

public class NotificationServiceModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(NotificationService.class).to(StandardNotificationService.class);
        expose(NotificationService.class);
    }

}
