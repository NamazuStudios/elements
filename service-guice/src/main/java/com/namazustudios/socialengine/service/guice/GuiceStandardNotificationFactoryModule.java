package com.namazustudios.socialengine.service.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.NotificationBuilder;
import com.namazustudios.socialengine.service.notification.NotificationDestinationFactory;
import com.namazustudios.socialengine.service.notification.NotificationFactory;
import com.namazustudios.socialengine.service.notification.StandardNotificationBuilder;
import com.namazustudios.socialengine.service.notification.StandardNotificationDestinationFactoryProvider;
import com.namazustudios.socialengine.service.notification.firebase.FirebaseMessagingFactory;
import com.namazustudios.socialengine.service.notification.firebase.FirebaseMessagingFactoryProvider;

public class GuiceStandardNotificationFactoryModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NotificationBuilder.class).to(StandardNotificationBuilder.class);
        bind(NotificationFactory.class).toProvider(StandardNotificationFactoryProvider.class);
        bind(NotificationDestinationFactory.class).toProvider(StandardNotificationDestinationFactoryProvider.class);
        bind(FirebaseMessagingFactory.class).toProvider(FirebaseMessagingFactoryProvider.class);
    }

}
