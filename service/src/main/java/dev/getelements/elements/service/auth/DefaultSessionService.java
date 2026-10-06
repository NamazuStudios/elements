package dev.getelements.elements.service.auth;

import dev.getelements.elements.sdk.dao.SessionDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.UnauthorizedException;
import dev.getelements.elements.sdk.model.exception.security.NoSessionException;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.auth.SessionTokenClaims;

import dev.getelements.elements.sdk.service.auth.SessionService;
import dev.getelements.elements.sdk.service.auth.SessionTokenVerifier;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import static dev.getelements.elements.sdk.service.Constants.SESSION_TIMEOUT_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DefaultSessionService implements SessionService {

    private SessionDao sessionDao;

    private SessionTokenVerifier sessionTokenVerifier;

    private long sessionTimeoutSeconds;

    @Override
    public Session checkAndRefreshSessionIfNecessary(final String sessionSecret) {

        // Signed session tokens carry their own expiry and are never refreshed: the token expires at
        // its expirationTime claim regardless of activity. Only legacy opaque secrets slide.

        if (getSessionTokenVerifier().isSessionToken(sessionSecret)) {
            return getSessionIfValid(sessionSecret);
        }

        final long expiry = MILLISECONDS.convert(getSessionTimeoutSeconds(), SECONDS) + currentTimeMillis();

        try {
            return getSessionDao().refresh(sessionSecret, expiry);
        } catch (NotFoundException ex) {
            throw new ForbiddenException(ex);
        }

    }

    @Override
    public void blacklistSession(final String sessionSecret) {

        if (getSessionTokenVerifier().isSessionToken(sessionSecret)) {
            final var claims = verify(sessionSecret);
            getSessionDao().deleteSessionBySessionId(claims.getSessionId());
        } else {
            getSessionDao().blacklist(sessionSecret);
        }

    }

    @Override
    public Session getSessionIfValid(final String sessionSecret) {

        if (getSessionTokenVerifier().isSessionToken(sessionSecret)) {
            final var claims = verify(sessionSecret);

            // A valid token whose record is missing has been revoked: that is an authentication
            // failure (401), not an authorization failure.

            try {
                return getSessionDao().getSessionBySessionId(claims.getSessionId());
            } catch (NoSessionException ex) {
                throw new UnauthorizedException(ex);
            }
        }

        try {
            return getSessionDao().getBySessionSecret(sessionSecret);
        } catch (NotFoundException ex) {
            throw new ForbiddenException(ex);
        }

    }

    private SessionTokenClaims verify(final String sessionToken) {
        return getSessionTokenVerifier().verify(sessionToken);
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public SessionTokenVerifier getSessionTokenVerifier() {
        return sessionTokenVerifier;
    }

    @Inject
    public void setSessionTokenVerifier(SessionTokenVerifier sessionTokenVerifier) {
        this.sessionTokenVerifier = sessionTokenVerifier;
    }

    public long getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Inject
    public void setSessionTimeoutSeconds(@Named(SESSION_TIMEOUT_SECONDS) long sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

}
