package dev.getelements.elements.appserve;

import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.rt.Request;
import dev.getelements.elements.rt.Response;
import dev.getelements.elements.rt.handler.Filter;
import dev.getelements.elements.security.SessionSecretHeader;
import dev.getelements.elements.service.SessionService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.getelements.elements.model.session.Session.SESSION_ATTRIBUTE;

public class RequestAttributeSessionFilter implements Filter {

    private SessionService sessionService;

    private SessionSecretHeader sessionSecretHeader;

    @Override
    public void filter(final Chain next,
                       final dev.getelements.elements.rt.handler.Session session,
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
