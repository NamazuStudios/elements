package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.service.profile.ProfileOverrideService;
import dev.getelements.elements.sdk.util.ElementScopes;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.model.Headers.PROFILE_ID;
import static dev.getelements.elements.sdk.model.profile.Profile.PROFILE_ATTRIBUTE;

public class HttpServletHeaderProfileOverrideFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletHeaderProfileOverrideFilter.class);

    private ElementRegistry registry;

    private Provider<ProfileOverrideService> profileOverrideServiceProvider;

    @Override
    public void doFilter(
            final ServletRequest _request,
            final ServletResponse _response,
            final FilterChain chain) throws IOException, ServletException {

        final var request = (HttpServletRequest) _request;

        final var profileOverrideService = getProfileOverrideServiceProvider().get();

        final var override = Optional
                .ofNullable(request.getHeader(PROFILE_ID))
                .flatMap(profileOverrideService::findOverrideProfile);

        if (override.isPresent()) {

            final var scopeAttributes = new SimpleAttributes.Builder()
                    .setAttribute(PROFILE_ATTRIBUTE, override.get())
                    .build();

            final var scopes = ElementScopes.builder()
                    .withLogger(logger)
                    .withRegistry(getRegistry())
                    .withNameFrom(HttpServletHeaderProfileOverrideFilter.class)
                    .withAttributes(scopeAttributes)
                    .withElementsNamed("dev.getelements.elements.sdk.service")
                    .build();

            try (var handle = scopes.enter()) {
                chain.doFilter(request, _response);
            }

        } else {
            chain.doFilter(_request, _response);
        }

    }

    public ElementRegistry getRegistry() {
        return registry;
    }

    @Inject
    public void setRegistry(@Named(ROOT) ElementRegistry registry) {
        this.registry = registry;
    }

    public Provider<ProfileOverrideService> getProfileOverrideServiceProvider() {
        return profileOverrideServiceProvider;
    }

    @Inject
    public void setProfileOverrideServiceProvider(Provider<ProfileOverrideService> profileOverrideServiceProvider) {
        this.profileOverrideServiceProvider = profileOverrideServiceProvider;
    }

}
