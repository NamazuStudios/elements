package com.namazustudios.socialengine.appserve.provider;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.security.SessionSecretHeader;

import javax.inject.Inject;
import javax.inject.Provider;

public class RequestSessionSecretHeaderProvider implements Provider<SessionSecretHeader> {

    private Request request;

    @Override
    public SessionSecretHeader get() {
        final RequestHeader header = getRequest().getHeader();
        return SessionSecretHeader.withOptionalValueSupplier(header::getHeader);
    }

    public Request getRequest() {
        return request;
    }

    @Inject
    public void setRequest(Request request) {
        this.request = request;
    }

}
