package com.namazustudios.socialengine.service.notification;

import com.google.api.core.ApiFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.namazustudios.socialengine.dao.FCMRegistrationDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.notification.firebase.FirebaseMessagingFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StandardNotificationDestinationProvider implements Provider<NotificationDestinationFactory> {

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
            final List<FCMRegistration> fcmRegistrationList = fcmRegistrationDao.getRegistrationsForRecipient(application.getId(), recipient.getId());

            return fcmRegistrationList.stream().map(fcmRegistration -> (p, success, failure) -> {

                final Message message = Message.builder()
                    .setNotification(new Notification(p.getTitle(), p.getMessage()))
                    .setToken(fcmRegistration.getRegistrationToken())
                    .build();

                final ApiFuture<String> apiFuture = firebaseMessaging.sendAsync(message);

                apiFuture.addListener(() -> {
                    try {

                        final String result = apiFuture.get();

                        if (TOKEN_NOT_REGISTERED.equals(result)) {
                            fcmRegistrationDao.deleteRegistration(fcmRegistration.getId());
                        }

                        if (result.startsWith(MESSAGE_ERROR_PREFIX)) {
                            failure.accept(new InternalException(result));
                        } else {
                            success.accept(() -> p);
                        }

                    } catch (ExecutionException e) {
                        failure.accept((Exception) e.getCause());
                    } catch (Exception ex) {
                        failure.accept(ex);
                    }
                }, MoreExecutors.directExecutor());

            });

        };

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
