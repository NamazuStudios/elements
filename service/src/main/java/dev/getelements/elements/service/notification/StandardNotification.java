package dev.getelements.elements.service.notification;

import dev.getelements.elements.sdk.service.notification.*;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

public class StandardNotification implements Notification {

    private NotificationParameters notificationParameters;

    private NotificationDestinationFactory notificationDestinationFactory;

    public void initialize(NotificationParameters notificationParameters) {
        this.notificationParameters = notificationParameters;
    }

    @Override
    public int send(final Consumer<NotificationEvent> success, final Consumer<Exception> failure) {
        return getNotificationDestinationFactory()
            .apply(getNotificationParameters())
            .peek(nd -> send(nd, success, failure))
            .mapToInt(nd -> 1)
            .reduce(0, (a, b) -> a + b);
    }

    private void send(final NotificationDestination destination,
                      final Consumer<NotificationEvent> success,
                      final Consumer<Exception> failure) {
        try {
            destination.send(getNotificationParameters(), success, failure);
        } catch (Exception ex) {
            failure.accept(ex);
        }
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
