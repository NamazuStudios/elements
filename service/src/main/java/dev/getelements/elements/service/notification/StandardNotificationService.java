package dev.getelements.elements.service.notification;

import dev.getelements.elements.sdk.model.profile.Profile;

import dev.getelements.elements.sdk.service.notification.NotificationBuilder;
import dev.getelements.elements.sdk.service.notification.NotificationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.function.Supplier;

public class StandardNotificationService implements NotificationService {

    private Supplier<Profile> currentProfileSupplier;

    private Provider<NotificationBuilder> notificationBuilderProvider;

    @Override
    public NotificationBuilder getBuilder() {
        return getNotificationBuilderProvider().get().sender(getCurrentProfileSupplier().get());
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
