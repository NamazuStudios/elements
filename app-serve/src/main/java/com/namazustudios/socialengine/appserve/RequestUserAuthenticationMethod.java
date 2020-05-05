package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.user.User.USER_ATTRIBUTE;

public class RequestUserAuthenticationMethod implements UserAuthenticationMethod {

    private Request request;

    @Override
    public User attempt() throws ForbiddenException {
        return getRequest()
            .getAttributes()
            .getAttributeOptional(USER_ATTRIBUTE)
            .map(User.class::cast)
            .orElseThrow(() -> new ForbiddenException());
    }

    public Request getRequest() {
        return request;
    }

    @Inject
    public void setRequest(Request request) {
        this.request = request;
    }

}
