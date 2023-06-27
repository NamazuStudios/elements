package dev.getelements.elements.rt.servlet;

import dev.getelements.elements.rt.Attributes;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

public class HttpServletAttributesProvider implements Provider<Attributes> {

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
