package com.namazustudios.socialengine.service.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.application.ApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.rt.util.ReadWriteGuard;
import com.namazustudios.socialengine.rt.util.ReentrantReadWriteGuard;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;

public class CachingFirebaseAppFactory implements FirebaseAppFactory {

    private FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao;

    private final Map<String, FirebaseApp> firebaseAppCache = new HashMap<>();

    private final ReadWriteGuard readWriteGuard = new ReentrantReadWriteGuard();

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

        final var parent = firebaseApplicationConfiguration.getParent();
        final var key = format("%s-%s", parent.getName(), parent.getId());

        final var app = readWriteGuard.computeRO(c -> firebaseAppCache.get(key));

        return app == null
            ? readWriteGuard.computeRW(c -> loadCredentialsAndReturnApp(firebaseApplicationConfiguration, key))
            : app;

    }

    private FirebaseApp loadCredentialsAndReturnApp(final FirebaseApplicationConfiguration firebaseApplicationConfiguration,
                                                    final String key) {

        final var credentials = firebaseApplicationConfiguration.getServiceAccountCredentials();

        final FirebaseOptions firebaseOptions;

        try (var credentialsStream = new ByteArrayInputStream(credentials.getBytes(StandardCharsets.UTF_8))) {
            firebaseOptions = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .build();
        } catch (IOException e) {
            throw new InternalException(e);
        }

        return firebaseAppCache.computeIfAbsent(key, k -> FirebaseApp.initializeApp(firebaseOptions, key));

    }

    public FirebaseApplicationConfigurationDao getFirebaseApplicationConfigurationDao() {
        return firebaseApplicationConfigurationDao;
    }

    @Inject
    public void setFirebaseApplicationConfigurationDao(FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao) {
        this.firebaseApplicationConfigurationDao = firebaseApplicationConfigurationDao;
    }

}
