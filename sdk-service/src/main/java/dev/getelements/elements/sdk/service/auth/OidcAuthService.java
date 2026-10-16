package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.OidcAuthLoginEvent;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
@ElementEventProducer(
        value = OidcAuthService.OIDC_AUTH_LOGIN_EVENT,
        parameters = OidcAuthLoginEvent.class,
        description = "Called when a user successfully authenticates via an OIDC provider, covering the " +
                "direct id_token login, the browser-redirect login, and the account-link flows. The event " +
                "carries the authenticating user, the profile attached to the session (if any), the validated " +
                "id_token (when a live token is available), the auth scheme, and the provider configuration " +
                "(when the login went through a configured provider)."
)
public interface OidcAuthService {

    /**
     * The name of the {@link dev.getelements.elements.sdk.Event} published when a user successfully
     * authenticates via an OIDC provider. The event carries a single
     * {@link OidcAuthLoginEvent} argument.
     */
    String OIDC_AUTH_LOGIN_EVENT = "dev.getelements.elements.sdk.service.auth.oidc.login.success";

    /**
     * Performs a sign-in with the supplied {@link OidcSessionRequest}.
     *
     * @param oidcSessionRequest the oidc session request object
     *
     * @return the {@link SessionCreation} if the session was created successfully.
     */
    SessionCreation createSession(OidcSessionRequest oidcSessionRequest);

}
