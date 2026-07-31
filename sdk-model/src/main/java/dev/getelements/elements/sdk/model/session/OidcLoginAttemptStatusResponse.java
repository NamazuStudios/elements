package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response body for {@code GET /oidc/session/{handle}}. */
@Schema(description = "The current state of a pending OIDC login attempt.")
public class OidcLoginAttemptStatusResponse {

    /** Creates a new instance. */
    public OidcLoginAttemptStatusResponse() {}

    @Schema(description = "The current state of the attempt.")
    private OidcLoginAttemptState status;

    @Schema(description = "The completed Elements session. Only set once, on the poll that observes COMPLETE.")
    private SessionCreation session;

    @Schema(description = "A human-readable failure reason. Only set when status is FAILED.")
    private String reason;

    public static OidcLoginAttemptStatusResponse pending() {
        final var response = new OidcLoginAttemptStatusResponse();
        response.setStatus(OidcLoginAttemptState.PENDING);
        return response;
    }

    public static OidcLoginAttemptStatusResponse complete(final SessionCreation session) {
        final var response = new OidcLoginAttemptStatusResponse();
        response.setStatus(OidcLoginAttemptState.COMPLETE);
        response.setSession(session);
        return response;
    }

    public static OidcLoginAttemptStatusResponse failed(final String reason) {
        final var response = new OidcLoginAttemptStatusResponse();
        response.setStatus(OidcLoginAttemptState.FAILED);
        response.setReason(reason);
        return response;
    }

    public static OidcLoginAttemptStatusResponse expired() {
        final var response = new OidcLoginAttemptStatusResponse();
        response.setStatus(OidcLoginAttemptState.EXPIRED);
        return response;
    }

    public OidcLoginAttemptState getStatus() {
        return status;
    }

    public void setStatus(OidcLoginAttemptState status) {
        this.status = status;
    }

    public SessionCreation getSession() {
        return session;
    }

    public void setSession(SessionCreation session) {
        this.session = session;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
