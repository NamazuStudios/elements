package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.util.ElementScopes;
import dev.getelements.elements.servlet.ServletRequestAttributes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

public class HttpServletElementScopeFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletElementScopeFilter.class);

    private ElementRegistry registry;

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws ServletException, IOException {

        final var httpServletRequestAttributes = new ServletRequestAttributes(servletRequest);

        final var scopes = ElementScopes.builder()
                .withLogger(logger)
                .withNameFrom(HttpServletElementScopeFilter.class)
                .withRegistry(getRegistry())
                .withAttributes(httpServletRequestAttributes)
                .build();

        try (final var handles = scopes.enter()) {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    public ElementRegistry getRegistry() {
        return registry;
    }

    @Inject
    public void setRegistry(@Named(ROOT) ElementRegistry registry) {
        this.registry = registry;
    }

}
