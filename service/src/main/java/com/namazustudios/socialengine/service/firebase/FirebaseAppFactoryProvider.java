package com.namazustudios.socialengine.service.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FirebaseAppFactoryProvider implements Provider<FirebaseAppFactory> {

    private Provider<FirebaseApplicationConfigurationDao> firebaseApplicationConfigurationDaoProvider;

    private final ConcurrentMap<String, FirebaseApp> firebaseAppCache = new ConcurrentHashMap<>();

    @Override
    public FirebaseAppFactory get() {
        final FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao;
        firebaseApplicationConfigurationDao = getFirebaseApplicationConfigurationDaoProvider().get();
        return application -> getFirebaseApp(firebaseApplicationConfigurationDao, application) ;
    }

    private FirebaseApp getFirebaseApp(final FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao,
                                       final Application application) {

        final FirebaseApplicationConfiguration firebaseApplicationConfiguration;
        try {
            firebaseApplicationConfiguration = firebaseApplicationConfigurationDao
                .getFirebaseApplicationConfigurationForApplication(application.getId());
        } catch (NotFoundException ex) {
            throw new InternalException(ex);
        }

        return firebaseAppCache.computeIfAbsent(application.getId(), applicationId -> loadCredentialsAndReturnApp(applicationId, firebaseApplicationConfiguration));

    }

    private FirebaseApp loadCredentialsAndReturnApp(
            final String applicationId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final String credentials = firebaseApplicationConfiguration.getServiceAccountCredentials();

        final FirebaseOptions firebaseOptions;

        try (final InputStream credentialsStream = new ByteArrayInputStream(credentials.getBytes(Charset.forName("UTF-8")))) {
            firebaseOptions = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .build();
        } catch (IOException e) {
            throw new InternalException(e);
        }

        return FirebaseApp.initializeApp(firebaseOptions, applicationId);

    }

    public Provider<FirebaseApplicationConfigurationDao> getFirebaseApplicationConfigurationDaoProvider() {
        return firebaseApplicationConfigurationDaoProvider;
    }

    @Inject
    public void setFirebaseApplicationConfigurationDaoProvider(Provider<FirebaseApplicationConfigurationDao> firebaseApplicationConfigurationDaoProvider) {
        this.firebaseApplicationConfigurationDaoProvider = firebaseApplicationConfigurationDaoProvider;
    }

}
