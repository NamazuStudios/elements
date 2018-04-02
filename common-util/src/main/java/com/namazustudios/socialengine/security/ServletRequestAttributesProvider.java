package com.namazustudios.socialengine.security;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.jar.Attributes;

public class ServletRequestAttributesProvider implements Provider<Attributes> {

    @Inject
    private Provider<HttpServletRequest> httpServletRequestProvider;

    @Override
    public Attributes get() {
        return null;
    }

}
