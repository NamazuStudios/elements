package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.Notification;
import com.namazustudios.socialengine.service.NotificationParameters;

import javax.inject.Inject;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class StandardNotification implements Notification {

    private final NotificationParameters notificationParameters;

    private NotificationDestinationFactory notificationDestinationFactory;

    public StandardNotification(final NotificationParameters notificationParameters) {
        this.notificationParameters = notificationParameters;
    }

    @Override
    public int send(final Consumer<NotificationEvent> success, final Consumer<Exception> failure) {
        return getNotificationDestinationFactory()
            .apply(getNotificationParameters())
            .reduce(0, (count, notificationDestination) -> {
                notificationDestination.send(getNotificationParameters(), success, failure);
                return 1;
            }, (a, b) -> a + b);
    }

    public NotificationParameters getNotificationParameters() {
        return notificationParameters;
    }

    public Function<NotificationParameters, Stream<NotificationDestination>> getNotificationDestinationFactory() {
        return notificationDestinationFactory;
    }

    @Inject
    public void setNotificationDestinationFactory(NotificationDestinationFactory notificationDestinationFactory) {
        this.notificationDestinationFactory = notificationDestinationFactory;
    }

}
