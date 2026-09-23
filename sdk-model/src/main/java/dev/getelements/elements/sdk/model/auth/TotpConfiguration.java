package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * Represents the single, system-wide TOTP (RFC 6238) two-factor authentication configuration. Off by default;
 * until this is enabled, no account's 2FA enrollment is enforced at login, even if that account has already
 * completed enrollment.
 */
@Schema(description = "The system-wide TOTP two-factor authentication configuration.")
public class TotpConfiguration {

    /** Creates a new instance. */
    public TotpConfiguration() {}

    @Schema(description = "The unique ID of the configuration.")
    private String id;

    @Schema(description = "Whether TOTP two-factor authentication is enforced. Off by default; enrolled " +
            "accounts are not challenged for a code at login until this is enabled.")
    private boolean enabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TotpConfiguration that = (TotpConfiguration) o;
        return enabled == that.enabled && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enabled);
    }

    @Override
    public String toString() {
        return "TotpConfiguration{id='" + id + "', enabled=" + enabled + '}';
    }

}
