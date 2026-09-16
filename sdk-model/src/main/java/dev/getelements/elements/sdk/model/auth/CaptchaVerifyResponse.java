package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/** The result of verifying a CAPTCHA response token. */
@Schema(description = "The result of verifying a CAPTCHA response token.")
public class CaptchaVerifyResponse {

    /** Creates a new instance. */
    public CaptchaVerifyResponse() {}

    @Schema(description = "Whether the CAPTCHA response token was verified successfully.")
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaptchaVerifyResponse that = (CaptchaVerifyResponse) o;
        return success == that.success;
    }

    @Override
    public int hashCode() {
        return Objects.hash(success);
    }

    @Override
    public String toString() {
        return "CaptchaVerifyResponse{success=" + success + '}';
    }

}
