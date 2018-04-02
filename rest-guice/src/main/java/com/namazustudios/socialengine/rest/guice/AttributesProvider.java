package com.namazustudios.socialengine.rest.guice;

import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.servlet.ServletRequestAttributes;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

public class AttributesProvider implements Provider<Attributes> {

    private Provider<HttpServletRequest> httpServletRequestProvider;

    @Override
    public Attributes get() {
        return new ServletRequestAttributes(httpServletRequestProvider::get);
    }

    public Provider<HttpServletRequest> getHttpServletRequestProvider() {
        return httpServletRequestProvider;
    }

    @Inject
    public void setHttpServletRequestProvider(Provider<HttpServletRequest> httpServletRequestProvider) {
        this.httpServletRequestProvider = httpServletRequestProvider;
    }

}
