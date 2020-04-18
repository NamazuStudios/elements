package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;

public class RequestAttributeProfileIdentificationMethod implements ProfileIdentificationMethod {

    private Request request;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {
        return getRequest().getHeader()
            .getHeader(SESSION_ATTRIBUTE, Session.class)
            .map(s -> s.getProfile()).orElseThrow(() -> new UnidentifiedProfileException());
    }

    public Request getRequest() {
        return request;
    }

    @Inject
    public void setRequest(Request request) {
        this.request = request;
    }

}
