package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.security.SessionSecretHeader;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;

public class RequestAttributeSessionFilter implements Filter {

    private SessionService sessionService;

    private SessionSecretHeader sessionSecretHeader;

    @Override
    public void filter(final Chain next,
                       final com.namazustudios.socialengine.rt.handler.Session session,
                       final Request request,
                       final Consumer<Response> responseReceiver) {

        getSessionSecretHeader()
            .getSessionSecret()
            .map(sessionSecret -> getSessionService().checkAndRefreshSessionIfNecessary(sessionSecret))
            .ifPresent(userSession -> request.getAttributes().setAttribute(SESSION_ATTRIBUTE, userSession));

        next.next(session, request, responseReceiver);

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
