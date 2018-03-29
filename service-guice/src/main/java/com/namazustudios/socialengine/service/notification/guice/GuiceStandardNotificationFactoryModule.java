package com.namazustudios.socialengine.service.notification.guice;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.namazustudios.socialengine.service.Notification;
import com.namazustudios.socialengine.service.NotificationBuilder;
import com.namazustudios.socialengine.service.firebase.FirebaseAppFactory;
import com.namazustudios.socialengine.service.firebase.FirebaseAppFactoryProvider;
import com.namazustudios.socialengine.service.notification.NotificationDestinationFactory;
import com.namazustudios.socialengine.service.notification.NotificationFactory;
import com.namazustudios.socialengine.service.notification.StandardNotificationBuilder;
import com.namazustudios.socialengine.service.notification.StandardNotificationDestinationProvider;

public class GuiceStandardNotificationFactoryModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new FactoryModuleBuilder()
            .implement(Notification.class, GuiceStandardNotification.class)
            .build(GuiceStandardNotificationFactory.class));

        bind(NotificationBuilder.class).to(StandardNotificationBuilder.class);
        bind(NotificationFactory.class).to(GuiceStandardNotificationFactory.class);
        bind(FirebaseAppFactory.class).toProvider(FirebaseAppFactoryProvider.class);
        bind(NotificationDestinationFactory.class).toProvider(StandardNotificationDestinationProvider.class);

        expose(NotificationBuilder.class);

    }

}
