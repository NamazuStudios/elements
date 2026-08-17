package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.session.OidcLoginAttemptBegin;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptCallbackResult;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptStatusResponse;
import dev.getelements.elements.sdk.service.auth.OidcLoginAttemptService;
import jakarta.inject.Inject;

/**
 * Anonymous (first-time login) browser-redirect OIDC attempt. Dispatched by
 * {@link OidcLoginAttemptServiceProvider} when the caller has no existing Elements session; see
 * {@link UserOidcLoginAttemptService} for the account-linking counterpart.
 */
public class AnonOidcLoginAttemptService implements OidcLoginAttemptService {

    private OidcLoginAttemptOperations oidcLoginAttemptOperations;

    @Override
    public OidcLoginAttemptBegin begin(final String provider) {
        return getOidcLoginAttemptOperations().begin(provider);
    }

    @Override
    public OidcLoginAttemptStatusResponse poll(final String id) {
        return getOidcLoginAttemptOperations().poll(id);
    }

    @Override
    public OidcLoginAttemptStatusResponse confirmLink(final String id, final String confirmToken) {
        return getOidcLoginAttemptOperations().confirmLink(id, confirmToken);
    }

    @Override
    public OidcLoginAttemptCallbackResult handleCallback(final String provider, final String code,
                                                           final String state, final String error) {
        return getOidcLoginAttemptOperations().handleCallback(provider, code, state, error);
    }

    public OidcLoginAttemptOperations getOidcLoginAttemptOperations() {
        return oidcLoginAttemptOperations;
    }

    @Inject
    public void setOidcLoginAttemptOperations(OidcLoginAttemptOperations oidcLoginAttemptOperations) {
        this.oidcLoginAttemptOperations = oidcLoginAttemptOperations;
    }

}
