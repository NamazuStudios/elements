package com.namazustudios.socialengine.service.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.application.ApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CachingFirebaseAppFactory implements FirebaseAppFactory {

    private FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao;

    private final ConcurrentMap<String, FirebaseApp> firebaseAppCache = new ConcurrentHashMap<>();

    @Override
    public FirebaseApp fromApplication(final Application application) {
        try {
            final var firebaseApplicationConfiguration = getFirebaseApplicationConfigurationDao()
                    .getDefaultFirebaseApplicationConfigurationForApplication(application.getId());
            return fromConfiguration(firebaseApplicationConfiguration);
        } catch (ApplicationConfigurationNotFoundException ex) {
            throw new InternalException(ex);
        }
    }

    @Override
    public FirebaseApp fromConfiguration(final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final var loaded = new ArrayList<FirebaseApp>();

        final var result = firebaseAppCache.computeIfAbsent(
            firebaseApplicationConfiguration.getId(), applicationConfigurationId -> {
                final var app = loadCredentialsAndReturnApp(firebaseApplicationConfiguration);
                loaded.add(app);
                return app;
            }
        );

        loaded.remove(result);
        loaded.forEach(FirebaseApp::delete);

        return result;

    }

    private FirebaseApp loadCredentialsAndReturnApp(final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final var credentials = firebaseApplicationConfiguration.getServiceAccountCredentials();

        final FirebaseOptions firebaseOptions;

        try (var credentialsStream = new ByteArrayInputStream(credentials.getBytes(StandardCharsets.UTF_8))) {
            firebaseOptions = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .build();
        } catch (IOException e) {
            throw new InternalException(e);
        }

        return FirebaseApp.initializeApp(firebaseOptions, firebaseApplicationConfiguration.getParent().getName());

    }

    public FirebaseApplicationConfigurationDao getFirebaseApplicationConfigurationDao() {
        return firebaseApplicationConfigurationDao;
    }

    @Inject
    public void setFirebaseApplicationConfigurationDao(FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao) {
        this.firebaseApplicationConfigurationDao = firebaseApplicationConfigurationDao;
    }

}
