package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.NotificationBuilder;
import com.namazustudios.socialengine.service.NotificationService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Supplier;

public class StandardNotificationService implements NotificationService {

    private Supplier<Profile> currentProfileSupplier;

    private Provider<NotificationBuilder> notificationBuilderProvider;

    @Override
    public NotificationBuilder getBuilder() {
        return getNotificationBuilderProvider().get().withSender(getCurrentProfileSupplier().get());
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    public Provider<NotificationBuilder> getNotificationBuilderProvider() {
        return notificationBuilderProvider;
    }

    @Inject
    public void setNotificationBuilderProvider(Provider<NotificationBuilder> notificationBuilderProvider) {
        this.notificationBuilderProvider = notificationBuilderProvider;
    }

}
