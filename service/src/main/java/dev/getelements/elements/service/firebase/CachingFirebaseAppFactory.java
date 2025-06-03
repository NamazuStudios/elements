package dev.getelements.elements.service.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import dev.getelements.elements.rt.util.ReadWriteGuard;
import dev.getelements.elements.rt.util.ReentrantReadWriteGuard;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.application.ApplicationConfigurationNotFoundException;
import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class CachingFirebaseAppFactory implements FirebaseAppFactory {

    private ApplicationConfigurationDao applicationConfigurationDao;

    private final Map<String, FirebaseApp> firebaseAppCache = new HashMap<>();

    private final ReadWriteGuard readWriteGuard = new ReentrantReadWriteGuard();

    @Override
    public FirebaseApp fromApplication(final Application application) {
        try {

            final FirebaseApplicationConfiguration firebaseApplicationConfiguration = getApplicationConfigurationDao()
                    .getDefaultApplicationConfigurationForApplication(
                            application.getId(),
                            FirebaseApplicationConfiguration.class
                    );

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

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
