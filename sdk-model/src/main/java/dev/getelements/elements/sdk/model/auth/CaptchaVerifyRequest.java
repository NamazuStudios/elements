package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Represents a request to verify a CAPTCHA response token, independent of any specific login flow. Any
 * Elements-backed frontend can call this to gate its own actions (e.g. signup) behind CAPTCHA.
 */
@Schema(description = "Represents a request to verify a CAPTCHA response token.")
public class CaptchaVerifyRequest {

    /** Creates a new instance. */
    public CaptchaVerifyRequest() {}

    @NotBlank
    @Schema(description = "The response token produced by the CAPTCHA widget after the user completes the " +
            "challenge.")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaptchaVerifyRequest that = (CaptchaVerifyRequest) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "CaptchaVerifyRequest{token='...'}";
    }

}
