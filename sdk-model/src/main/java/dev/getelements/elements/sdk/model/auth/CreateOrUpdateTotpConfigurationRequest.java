package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/** Represents a request to create or update the system-wide {@link TotpConfiguration}. */
@Schema(description = "Represents a request to create or update the system-wide TOTP configuration.")
public class CreateOrUpdateTotpConfigurationRequest {

    /** Creates a new instance. */
    public CreateOrUpdateTotpConfigurationRequest() {}

    @Schema(description = "Whether TOTP two-factor authentication should be enforced.")
    private boolean enabled;

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
        CreateOrUpdateTotpConfigurationRequest that = (CreateOrUpdateTotpConfigurationRequest) o;
        return enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled);
    }

    @Override
    public String toString() {
        return "CreateOrUpdateTotpConfigurationRequest{enabled=" + enabled + '}';
    }

}
