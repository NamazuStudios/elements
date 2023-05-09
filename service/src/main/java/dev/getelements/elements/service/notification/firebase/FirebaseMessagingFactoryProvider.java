package dev.getelements.elements.service.notification.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import dev.getelements.elements.service.firebase.FirebaseAppFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class FirebaseMessagingFactoryProvider implements Provider<FirebaseMessagingFactory> {

    private Provider<FirebaseAppFactory> firebaseAppFactoryProvider;

    @Override
    public FirebaseMessagingFactory get() {
        return application -> {
            final FirebaseApp firebaseApp = getFirebaseAppFactoryProvider().get().fromApplication(application);
            return FirebaseMessaging.getInstance(firebaseApp);
        };
    }

    public Provider<FirebaseAppFactory> getFirebaseAppFactoryProvider() {
        return firebaseAppFactoryProvider;
    }

    @Inject
    public void setFirebaseAppFactoryProvider(final Provider<FirebaseAppFactory> firebaseAppFactoryProvider) {
        this.firebaseAppFactoryProvider = firebaseAppFactoryProvider;
    }

}
