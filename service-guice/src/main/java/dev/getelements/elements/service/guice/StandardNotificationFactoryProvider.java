package dev.getelements.elements.service.guice;


import com.google.inject.Injector;
import dev.getelements.elements.sdk.service.notification.NotificationFactory;
import dev.getelements.elements.service.notification.StandardNotification;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class StandardNotificationFactoryProvider implements Provider<NotificationFactory> {

    private Injector injector;

    public StandardNotificationFactoryProvider(Injector injector) {
        this.injector = injector;
    }

    @Override
    public NotificationFactory get() {
        return p -> {
            final StandardNotification standardNotification = new StandardNotification(p);
            getInjector().injectMembers(standardNotification);
            return standardNotification;
        };
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

}
