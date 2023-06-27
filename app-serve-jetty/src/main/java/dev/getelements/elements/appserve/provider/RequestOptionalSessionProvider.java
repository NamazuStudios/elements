package dev.getelements.elements.appserve.provider;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.security.SessionExpiredException;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.security.SessionSecretHeader;
import dev.getelements.elements.service.SessionService;
import dev.getelements.elements.service.Unscoped;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;

public class RequestOptionalSessionProvider implements Provider<Optional<Session>> {

    private SessionService sessionService;

    private SessionSecretHeader sessionSecretHeader;

    @Override
    public Optional<Session> get() {
        return getSessionSecretHeader()
            .getSessionSecret()
            .flatMap(secret -> checkAndRefreshSession(secret));
    }

    private Optional<Session> checkAndRefreshSession(String secret) {
        try {
            return Optional.of(getSessionService().checkAndRefreshSessionIfNecessary(secret));
        } catch (SessionExpiredException | ForbiddenException ex) {
            return Optional.empty();
        }
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public SessionSecretHeader getSessionSecretHeader() {
        return sessionSecretHeader;
    }

    @Inject
    public void setSessionSecretHeader(SessionSecretHeader sessionSecretHeader) {
        this.sessionSecretHeader = sessionSecretHeader;
    }

}
