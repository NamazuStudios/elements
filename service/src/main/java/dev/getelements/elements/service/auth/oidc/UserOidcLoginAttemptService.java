package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.model.session.OidcLoginAttemptBegin;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptCallbackResult;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptStatusResponse;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.OidcLoginAttemptService;
import jakarta.inject.Inject;

/**
 * Account-linking browser-redirect OIDC attempt, dispatched by {@link OidcLoginAttemptServiceProvider} when the
 * caller already has an Elements session. {@link #begin} stamps the pending attempt with the current user's id,
 * so the outcome (link to this user, rather than create/find one by external id) is decided and persisted here,
 * at the one point in the flow where the caller's session is actually known. The provider's callback request
 * itself carries no {@code Authorization} header of its own — it's a bare redirect from the IdP, not a call from
 * the original caller — so it relies entirely on that persisted decision rather than re-deriving it. {@code
 * poll}/{@code handleCallback} therefore need no user-level branching of their own.
 */
public class UserOidcLoginAttemptService implements OidcLoginAttemptService {

    private User user;

    private OidcLoginAttemptOperations oidcLoginAttemptOperations;

    @Override
    public OidcLoginAttemptBegin begin(final String provider) {
        return getOidcLoginAttemptOperations().begin(provider, getUser());
    }

    @Override
    public OidcLoginAttemptStatusResponse poll(final String id) {
        return getOidcLoginAttemptOperations().poll(id);
    }

    @Override
    public OidcLoginAttemptCallbackResult handleCallback(final String provider, final String code,
                                                           final String state, final String error) {
        return getOidcLoginAttemptOperations().handleCallback(provider, code, state, error);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public OidcLoginAttemptOperations getOidcLoginAttemptOperations() {
        return oidcLoginAttemptOperations;
    }

    @Inject
    public void setOidcLoginAttemptOperations(OidcLoginAttemptOperations oidcLoginAttemptOperations) {
        this.oidcLoginAttemptOperations = oidcLoginAttemptOperations;
    }

}
