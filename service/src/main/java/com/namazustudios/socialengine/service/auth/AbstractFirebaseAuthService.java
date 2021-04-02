package com.namazustudios.socialengine.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserInfo;
import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.FirebaseUserDao;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.application.ApplicationConfigurationNotFoundException;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.FirebaseAuthService;
import com.namazustudios.socialengine.service.firebase.FirebaseAppFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

import static com.namazustudios.socialengine.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class AbstractFirebaseAuthService implements FirebaseAuthService {

    private SessionDao sessionDao;

    private FirebaseUserDao firebaseUserDao;

    private FirebaseAppFactory firebaseAppFactory;

    private FirebaseApplicationConfigurationDao firebaseApplicationConfigurationDao;

    private long sessionTimeoutSeconds;

    @Override
    public SessionCreation createOrUpdateUserWithFirebaseJWT(final String firebaseJWT) {
        try {

            final var decoded = JWT.decode(firebaseJWT);

            return decoded.getAudience()
                .stream()
                .map(audience -> getFirebaseApplicationConfigurationDao().getApplicationConfiguration(audience))
                .filter(Objects::nonNull)
                .map(fac -> initSession(fac, decoded))
                .findFirst()
                .orElseThrow(ForbiddenException::new);

        } catch (JWTDecodeException | ApplicationConfigurationNotFoundException ex) {
            throw new ForbiddenException(ex);
        }
    }

    private SessionCreation initSession(final FirebaseApplicationConfiguration firebaseApplicationConfiguration,
                                        final DecodedJWT jwt) {

        final var firebaseApp = getFirebaseAppFactory().fromConfiguration(firebaseApplicationConfiguration);
        final var auth = FirebaseAuth.getInstance(firebaseApp);

        try {

            final var uid = jwt.getClaim("user_id").asString();
            auth.verifyIdToken(jwt.getToken());

            final var userInfo = auth.getUser(uid);
            final var user = getUserFromUserInfo(userInfo);

            final var session = new Session();
            final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();

            session.setUser(user);
            session.setExpiry(expiry);
            session.setApplication(firebaseApplicationConfiguration.getParent());

            return getSessionDao().create(session);

        } catch (FirebaseAuthException ex) {
            throw new ForbiddenException(ex);
        }

    }

    protected abstract User getUserFromUserInfo(UserInfo userInfo) throws FirebaseAuthException;

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

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

}
