package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;

import javax.inject.Inject;
import java.util.Optional;

public class SessionUserAuthenticationMethod implements UserAuthenticationMethod {

    private Optional<Session> optionalSession;

    @Override
    public User attempt() throws ForbiddenException {
        return getOptionalSession().map(session -> session.getUser()).orElseThrow(ForbiddenException::new);
    }

    public Optional<Session> getOptionalSession() {
        return optionalSession;
    }

    @Inject
    public void setOptionalSession(Optional<Session> optionalSession) {
        this.optionalSession = optionalSession;
    }

}
