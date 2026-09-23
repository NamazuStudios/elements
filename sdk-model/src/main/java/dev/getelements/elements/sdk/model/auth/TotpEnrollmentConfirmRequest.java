package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/** Represents a request to confirm a pending TOTP enrollment with a code from the authenticator app. */
@Schema(description = "Represents a request to confirm a pending TOTP enrollment.")
public class TotpEnrollmentConfirmRequest {

    /** Creates a new instance. */
    public TotpEnrollmentConfirmRequest() {}

    @NotBlank
    @Schema(description = "The current code shown in the authenticator app.")
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TotpEnrollmentConfirmRequest that = (TotpEnrollmentConfirmRequest) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "TotpEnrollmentConfirmRequest{code='...'}";
    }

}
