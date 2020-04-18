package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import com.namazustudios.socialengine.security.SessionSecretHeader;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.model.User.USER_ATTRIBUTE;
import static com.namazustudios.socialengine.model.profile.Profile.PROFILE_ATTRIBUTE;
import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;
import static com.namazustudios.socialengine.security.SessionSecretHeader.SESSION_SECRET_HEADER_ATTRIBUTE;

public class SessionIdAuthenticationFilter implements Filter {

    private SessionService sessionService;

    @Override
    public void filter(final Chain next,
                       final Session session,
                       final Request request,
                       final Consumer<Response> responseReceiver) {

        final RequestHeader header = request.getHeader();
        final SessionSecretHeader parsed = new SessionSecretHeader(header::getHeader);

        if (parsed.getSessionSecret().isPresent()) {

            request.getAttributes().setAttribute(SESSION_SECRET_HEADER_ATTRIBUTE, parsed);

            final com.namazustudios.socialengine.model.session.Session userSession;
            userSession = getSessionService().checkAndRefreshSessionIfNecessary(parsed.getSessionSecret().get());

            final User user = userSession.getUser();
            request.getAttributes().setAttribute(SESSION_ATTRIBUTE, userSession);

            final Profile profile = userSession.getProfile();

            if (user != null) request.getAttributes().setAttribute(USER_ATTRIBUTE, user);
            if (profile != null) request.getAttributes().setAttribute(PROFILE_ATTRIBUTE, profile);

        }

        next.next(session, request, responseReceiver);

    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

}
