package dev.getelements.elements.service.auth.captcha;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Maps the JSON response body of Google reCAPTCHA's {@code siteverify} endpoint. */
public class RecaptchaSiteVerifyResponse {

    private boolean success;

    @JsonProperty("challenge_ts")
    private String challengeTimestamp;

    private String hostname;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getChallengeTimestamp() {
        return challengeTimestamp;
    }

    public void setChallengeTimestamp(String challengeTimestamp) {
        this.challengeTimestamp = challengeTimestamp;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }

    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }

}
