package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.service.auth.OidcLoginAttemptService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Always anonymous, mirroring {@link OidcAuthServiceProvider} — a caller has no Elements session yet when
 * starting or polling a browser-redirect login attempt, by definition, so there is no per-user-level dispatch.
 */
public class OidcLoginAttemptServiceProvider implements Provider<OidcLoginAttemptService> {

    private Provider<StandardOidcLoginAttemptService> standardOidcLoginAttemptServiceProvider;

    @Override
    public OidcLoginAttemptService get() {
        return getStandardOidcLoginAttemptServiceProvider().get();
    }

    public Provider<StandardOidcLoginAttemptService> getStandardOidcLoginAttemptServiceProvider() {
        return standardOidcLoginAttemptServiceProvider;
    }

    @Inject
    public void setStandardOidcLoginAttemptServiceProvider(Provider<StandardOidcLoginAttemptService> standardOidcLoginAttemptServiceProvider) {
        this.standardOidcLoginAttemptServiceProvider = standardOidcLoginAttemptServiceProvider;
    }

}
