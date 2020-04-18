package com.namazustudios.socialengine.service.guice;


import com.google.inject.Injector;
import com.namazustudios.socialengine.service.notification.NotificationFactory;
import com.namazustudios.socialengine.service.notification.StandardNotification;

import javax.inject.Inject;
import javax.inject.Provider;

public class StandardNotificationFactoryProvider implements Provider<NotificationFactory> {

    private Injector injector;

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
