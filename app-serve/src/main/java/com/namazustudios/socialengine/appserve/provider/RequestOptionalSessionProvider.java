package com.namazustudios.socialengine.appserve.provider;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.security.SessionExpiredException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.security.SessionSecretHeader;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.Unscoped;

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
