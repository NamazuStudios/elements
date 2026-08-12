package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;

/** The API-facing state of a pending browser-redirect OIDC login attempt. */
@Schema(description = "The state of a pending OIDC login attempt.")
public enum OidcLoginAttemptState {

    /** Still waiting on the provider's redirect/callback. */
    PENDING,

    /** The session was created successfully. Returned exactly once. */
    COMPLETE,

    /** The login was denied or failed validation. */
    FAILED,

    /** The attempt is unknown or has passed its TTL. */
    EXPIRED

}
