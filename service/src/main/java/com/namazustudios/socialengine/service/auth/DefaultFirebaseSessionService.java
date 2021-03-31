package com.namazustudios.socialengine.service.auth;

import com.auth0.jwt.JWT;
import com.google.firebase.auth.FirebaseAuth;
import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.FirebaseUserDao;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.FirebaseSessionService;
import com.namazustudios.socialengine.service.firebase.FirebaseAppFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class DefaultFirebaseSessionService implements FirebaseSessionService {

    private FirebaseUserDao firebaseUserDao;

    private FirebaseAppFactory firebaseAppFactory;

    private FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao;

    @Override
    public Optional<Session> attemptVerification(final String sessionSecret) {
        final var decoded = JWT.decode(sessionSecret);
        return decoded.getAudience()
            .stream()
            .map(this::lookupAudience)
            .filter(Objects::nonNull)
            .map(fac -> initSession(fac, sessionSecret))
            .findFirst();
    }

    private FirebaseApplicationConfiguration lookupAudience(final String audience) {
        // TODO
        return null;
    }

    private Session initSession(final FirebaseApplicationConfiguration firebaseApplicationConfiguration,
                                final String sessionSecret) {
        // TODO
        return null;
    }

    public FirebaseUserDao getFirebaseUserDao() {
        return firebaseUserDao;
    }

    @Inject
    public void setFirebaseUserDao(FirebaseUserDao firebaseUserDao) {
        this.firebaseUserDao = firebaseUserDao;
    }

    public FirebaseAppFactory getFirebaseAppFactory() {
        return firebaseAppFactory;
    }

    @Inject
    public void setFirebaseAppFactory(FirebaseAppFactory firebaseAppFactory) {
        this.firebaseAppFactory = firebaseAppFactory;
    }

    public FirebaseApplicationConfigurationDao getFirebaseApplicationConfigurationDao() {
        return firebaseApplicationConfigurationDao;
    }

    @Inject
    public void setFirebaseApplicationConfigurationDao(FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao) {
        this.firebaseApplicationConfigurationDao = firebaseApplicationConfigurationDao;
    }

}
