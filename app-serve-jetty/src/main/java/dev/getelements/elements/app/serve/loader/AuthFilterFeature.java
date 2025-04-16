package dev.getelements.elements.app.serve.loader;

import dev.getelements.elements.servlet.security.HttpServletAuthenticationFilter;
import dev.getelements.elements.servlet.security.HttpServletElementScopeFilter;
import dev.getelements.elements.servlet.security.HttpServletHeaderProfileOverrideFilter;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;

import java.util.function.Consumer;

import static java.util.EnumSet.allOf;

public class AuthFilterFeature implements Consumer<ServletContextHandler> {

    private Provider<HttpServletElementScopeFilter> httpServletElementScopeFilterProvider;

    private Provider<HttpServletAuthenticationFilter> httpServletAuthenticationFilterProvider;

    private Provider<HttpServletHeaderProfileOverrideFilter> httpServletHeaderProfileOverrideFilterProvider;

    @Override
    public void accept(final ServletContextHandler servletContextHandler) {
        final var scopeFilter = getHttpServletElementScopeFilterProvider().get();
        final var authFilter = getHttpServletAuthenticationFilterProvider().get();
        final var overrideFilter = getHttpServletHeaderProfileOverrideFilterProvider().get();
        servletContextHandler.addFilter(scopeFilter, "/*", allOf(DispatcherType.class));
        servletContextHandler.addFilter(authFilter, "/*", allOf(DispatcherType.class));
        servletContextHandler.addFilter(overrideFilter, "/*", allOf(DispatcherType.class));
    }

    public Provider<HttpServletElementScopeFilter> getHttpServletElementScopeFilterProvider() {
        return httpServletElementScopeFilterProvider;
    }

    @Inject
    public void setHttpServletElementScopeFilterProvider(Provider<HttpServletElementScopeFilter> httpServletElementScopeFilterProvider) {
        this.httpServletElementScopeFilterProvider = httpServletElementScopeFilterProvider;
    }

    public Provider<HttpServletAuthenticationFilter> getHttpServletAuthenticationFilterProvider() {
        return httpServletAuthenticationFilterProvider;
    }

    @Inject
    public void setHttpServletAuthenticationFilterProvider(Provider<HttpServletAuthenticationFilter> httpServletAuthenticationFilterProvider) {
        this.httpServletAuthenticationFilterProvider = httpServletAuthenticationFilterProvider;
    }

    public Provider<HttpServletHeaderProfileOverrideFilter> getHttpServletHeaderProfileOverrideFilterProvider() {
        return httpServletHeaderProfileOverrideFilterProvider;
    }

    @Inject
    public void setHttpServletHeaderProfileOverrideFilterProvider(Provider<HttpServletHeaderProfileOverrideFilter> httpServletHeaderProfileOverrideFilterProvider) {
        this.httpServletHeaderProfileOverrideFilterProvider = httpServletHeaderProfileOverrideFilterProvider;
    }

}
