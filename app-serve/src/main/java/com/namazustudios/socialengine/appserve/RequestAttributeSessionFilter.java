package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.handler.Filter;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;

public class RequestAttributeSessionFilter implements Filter {

    private Optional<Session> optionalSession;

    @Override
    public void filter(final Chain next,
                       final com.namazustudios.socialengine.rt.handler.Session session,
                       final Request request,
                       final Consumer<Response> responseReceiver) {
        getOptionalSession().ifPresent(s -> request.getAttributes().setAttribute(SESSION_ATTRIBUTE, s));
        next.next(session, request, responseReceiver);
    }

    public Optional<Session> getOptionalSession() {
        return optionalSession;
    }

    @Inject
    public void setOptionalSession(Optional<Session> optionalSession) {
        this.optionalSession = optionalSession;
    }

}
