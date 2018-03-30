package com.namazustudios.socialengine.service.notification.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.service.NotificationBuilder;
import com.namazustudios.socialengine.service.notification.*;
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
