package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.util.FinallyAction;
import dev.getelements.elements.servlet.ServletRequestAttributes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

public class HttpServletServicesScopeFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletServicesScopeFilter.class);

    private ElementRegistry registry;

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws ServletException, IOException {

        final var httpServletRequestAttributes = new ServletRequestAttributes(servletRequest);

        try (final var handles = enterScope(httpServletRequestAttributes)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    private FinallyAction enterScope(final Attributes attributes) {
        return getRegistry()
                .find("dev.getelements.elements.sdk.service")
                .map(element -> element.withScope().with(attributes).enter())
                .map(handle -> FinallyAction.begin(logger).thenClose(handle))
                .reduce(FinallyAction.begin(logger), (a, b) -> a.then(b));
    }

    public ElementRegistry getRegistry() {
        return registry;
    }

    @Inject
    public void setRegistry(@Named(ROOT) ElementRegistry registry) {
        this.registry = registry;
    }

}
