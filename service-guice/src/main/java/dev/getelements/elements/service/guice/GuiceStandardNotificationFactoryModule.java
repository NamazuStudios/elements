package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.service.NotificationBuilder;
import dev.getelements.elements.service.notification.NotificationDestinationFactory;
import dev.getelements.elements.service.notification.NotificationFactory;
import dev.getelements.elements.service.notification.StandardNotificationBuilder;
import dev.getelements.elements.service.notification.StandardNotificationDestinationFactoryProvider;
import dev.getelements.elements.service.notification.firebase.FirebaseMessagingFactory;
import dev.getelements.elements.service.notification.firebase.FirebaseMessagingFactoryProvider;

public class GuiceStandardNotificationFactoryModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NotificationBuilder.class).to(StandardNotificationBuilder.class);
        bind(NotificationFactory.class).toProvider(StandardNotificationFactoryProvider.class);
        bind(NotificationDestinationFactory.class).toProvider(StandardNotificationDestinationFactoryProvider.class);
        bind(FirebaseMessagingFactory.class).toProvider(FirebaseMessagingFactoryProvider.class);
    }

}
