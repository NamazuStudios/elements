package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * Returned when beginning TOTP enrollment. The enrollment is pending -- not yet enforced at login -- until
 * confirmed with a valid code via the confirm endpoint.
 */
@Schema(description = "A pending TOTP enrollment, awaiting confirmation with a valid code.")
public class TotpEnrollment {

    /** Creates a new instance. */
    public TotpEnrollment() {}

    @Schema(description = "The Base32-encoded shared secret, for manual entry into an authenticator app that " +
            "can't scan a QR code.")
    private String secret;

    @Schema(description = "The otpauth:// provisioning URI. Render this as a QR code for scanning by an " +
            "authenticator app (Google Authenticator, Authy, etc.).")
    private String otpAuthUri;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getOtpAuthUri() {
        return otpAuthUri;
    }

    public void setOtpAuthUri(String otpAuthUri) {
        this.otpAuthUri = otpAuthUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TotpEnrollment that = (TotpEnrollment) o;
        return Objects.equals(secret, that.secret) && Objects.equals(otpAuthUri, that.otpAuthUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secret, otpAuthUri);
    }

    @Override
    public String toString() {
        return "TotpEnrollment{...}";
    }

}
