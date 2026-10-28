package dev.getelements.elements.sdk.service.auth;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthLoginEvent;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;


@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
@ElementEventProducer(
        value = OAuth2AuthService.OAUTH2_AUTH_LOGIN_EVENT,
        parameters = OAuth2AuthLoginEvent.class,
        description = "Called when a user successfully authenticates via an OAuth2 auth scheme, covering " +
                "both the login and account-link flows. The event carries the authenticating user, the " +
                "profile attached to the session (if any), the auth scheme, and the credential material " +
                "the client presented (including the access token used for validation)."
)
public interface OAuth2AuthService {

    /**
     * The name of the {@link dev.getelements.elements.sdk.Event} published when a user successfully
     * authenticates via an OAuth2 auth scheme. The event carries a single
     * {@link OAuth2AuthLoginEvent} argument.
     */
    String OAUTH2_AUTH_LOGIN_EVENT = "dev.getelements.elements.sdk.service.auth.oauth2.login.success";

    /**
     * Performs a sign-in with the supplied {@link OAuth2SessionRequest}.
     *
     * @param oAuth2SessionRequest the oidc session request object
     *
     * @return the {@link SessionCreation} if the session was created successfully.
     */
    SessionCreation createSession(OAuth2SessionRequest oAuth2SessionRequest);

}
