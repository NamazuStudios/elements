package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import com.namazustudios.socialengine.service.SessionService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.model.user.User.USER_ATTRIBUTE;

public class RequestAttributeUserFilter implements Filter {

    private Provider<User> userProvider;

    private Provider<Session> sessionProvider;

    private SessionService sessionService;

    @Override
    public void filter(final Chain next,
                       final Session session,
                       final Request request,
                       final Consumer<Response> responseReceiver) {
        final User user = getUserProvider().get();
        request.getAttributes().setAttribute(USER_ATTRIBUTE, user);
        next.next(session, request, responseReceiver);
    }

    public Provider<User> getUserProvider() {
        return userProvider;
    }

    @Inject
    public void setUserProvider(Provider<User> userProvider) {
        this.userProvider = userProvider;
    }

    public SessionService getSessionService() {
        return sessionService;
    }

    @Inject
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

}
