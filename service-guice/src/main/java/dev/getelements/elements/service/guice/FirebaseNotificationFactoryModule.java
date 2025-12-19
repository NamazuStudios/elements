package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import dev.getelements.elements.sdk.service.notification.NotificationBuilder;
import dev.getelements.elements.sdk.service.notification.NotificationDestinationFactory;
import dev.getelements.elements.sdk.service.notification.NotificationFactory;
import dev.getelements.elements.service.notification.StandardNotificationBuilder;
import dev.getelements.elements.service.notification.StandardNotificationDestinationFactoryProvider;
import dev.getelements.elements.service.notification.firebase.FirebaseMessagingFactory;
import dev.getelements.elements.service.notification.firebase.FirebaseMessagingFactoryProvider;

public class FirebaseNotificationFactoryModule extends AbstractModule {

    @Override
    protected void configure() {
        final var injectorProvider = getProvider(Injector.class);
        bind(NotificationBuilder.class).to(StandardNotificationBuilder.class);
        bind(NotificationFactory.class).toProvider(new StandardNotificationFactoryProvider(injectorProvider.get()));
        bind(NotificationDestinationFactory.class).toProvider(StandardNotificationDestinationFactoryProvider.class);
        bind(FirebaseMessagingFactory.class).toProvider(FirebaseMessagingFactoryProvider.class);
    }

}
