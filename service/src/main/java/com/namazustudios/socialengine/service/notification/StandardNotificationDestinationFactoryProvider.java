package com.namazustudios.socialengine.service.notification;

import com.google.api.core.ApiFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.*;
import com.namazustudios.socialengine.dao.FCMRegistrationDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.NotificationEvent;
import com.namazustudios.socialengine.service.NotificationParameters;
import com.namazustudios.socialengine.service.notification.firebase.FirebaseMessagingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class StandardNotificationDestinationFactoryProvider implements Provider<NotificationDestinationFactory> {

    private static final Logger logger = LoggerFactory.getLogger(StandardNotificationDestinationFactoryProvider.class);

    private static final String MESSAGE_ERROR_PREFIX = "messaging";

    private static final String TOKEN_NOT_REGISTERED = "messaging/registration-token-not-registered";

    private Provider<FCMRegistrationDao> fcmRegistrationDaoProvider;

    private Provider<FirebaseMessagingFactory> firebaseMessagingFactoryProvider;

    @Override
    public NotificationDestinationFactory get() {

        final FCMRegistrationDao fcmRegistrationDao = getFcmRegistrationDaoProvider().get();
        final FirebaseMessagingFactory firebaseMessagingFactory = getFirebaseMessagingFactoryProvider().get();

        return parameters -> {

            final Profile recipient = parameters.getRecipient();
            final Application application = parameters.getApplication();

            final FirebaseMessaging firebaseMessaging = firebaseMessagingFactory.apply(application);

            final Stream<FCMRegistration> fcmRegistrationList;
            fcmRegistrationList = fcmRegistrationDao.getDistinctRegistrationsForRecipient(recipient.getId());

            return fcmRegistrationList.map(fcmRegistration -> (p, success, failure) -> {

                final Message message = Message.builder()
                    .setNotification(new Notification(p.getTitle(), p.getMessage()))
                    .setToken(fcmRegistration.getRegistrationToken())
                    .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder()
                            .setSound(p.getSound() == null ? "default" : p.getSound())
                            .setTitle(p.getTitle())
                            .setBody(p.getMessage())
                            .build())
                        .build())
                    .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                            .setSound(p.getSound() == null ? "default" : p.getSound())
                        .build())
                    .build())
                .build();

                final ApiFuture<String> apiFuture = firebaseMessaging.sendAsync(message);

                apiFuture.addListener(
                    () -> handle(apiFuture, p, fcmRegistrationDao, fcmRegistration, success, failure),
                    MoreExecutors.directExecutor());

            });

        };

    }

    private void handle(final ApiFuture<String> apiFuture,
                            final NotificationParameters parameters,
                            final FCMRegistrationDao fcmRegistrationDao,
                            final FCMRegistration fcmRegistration,
                            final Consumer<NotificationEvent> success,
                            final Consumer<Exception> failure) {
        try {

            final String result = apiFuture.get();

            if (result.startsWith(MESSAGE_ERROR_PREFIX)) {
                failure.accept(new InternalException(result));
            } else {
                final NotificationEvent ev = new StandardNotificationEvent(fcmRegistration.getId(), parameters);
                success.accept(ev);
            }

        } catch (ExecutionException e) {
            if (e.getCause() instanceof FirebaseMessagingException) {
                final FirebaseMessagingException fex = (FirebaseMessagingException) e.getCause();
                processFirebaseError(fex.getErrorCode(), fcmRegistrationDao, fcmRegistration);
                failure.accept(fex);
            } else if (e.getCause() instanceof Exception) {
                failure.accept((Exception)e.getCause());
            } else {
                failure.accept(new InternalException(e.getCause()));
            }
        } catch (Exception ex) {
            failure.accept(ex);
        }
    }

    private void processFirebaseError(final String result,
                                      final FCMRegistrationDao fcmRegistrationDao,
                                      final FCMRegistration fcmRegistration) {
        if (TOKEN_NOT_REGISTERED.equals(result)) {
            safeDeleteRegistration(fcmRegistrationDao, fcmRegistration);
        }
    }

    private void safeDeleteRegistration(final FCMRegistrationDao fcmRegistrationDao,
                                        final FCMRegistration fcmRegistration) {
        try {
            fcmRegistrationDao.deleteRegistration(fcmRegistration.getId());
        } catch (Exception ex) {
            logger.error("Failure to delete stale FCM registration.", ex);
        }
    }

    public Provider<FCMRegistrationDao> getFcmRegistrationDaoProvider() {
        return fcmRegistrationDaoProvider;
    }

    @Inject
    public void setFcmRegistrationDaoProvider(Provider<FCMRegistrationDao> fcmRegistrationDaoProvider) {
        this.fcmRegistrationDaoProvider = fcmRegistrationDaoProvider;
    }

    public Provider<FirebaseMessagingFactory> getFirebaseMessagingFactoryProvider() {
        return firebaseMessagingFactoryProvider;
    }

    @Inject
    public void setFirebaseMessagingFactoryProvider(Provider<FirebaseMessagingFactory> firebaseMessagingFactoryProvider) {
        this.firebaseMessagingFactoryProvider = firebaseMessagingFactoryProvider;
    }

}
