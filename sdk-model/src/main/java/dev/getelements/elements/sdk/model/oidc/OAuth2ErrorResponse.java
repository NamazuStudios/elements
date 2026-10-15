package dev.getelements.elements.sdk.model.oidc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * An RFC 6749 section 5.2 error response body, returned by the OIDC provider's token endpoint when a request
 * fails. The {@code error} value is one of the well-known OAuth 2.0 error codes (e.g. {@code invalid_grant},
 * {@code invalid_client}); {@code error_description} is a human-readable explanation.
 */
@Schema(description = "An OAuth 2.0 error response, per RFC 6749 section 5.2.")
public class OAuth2ErrorResponse {

    @Schema(description = "The OAuth 2.0 error code, e.g. \"invalid_grant\".")
    @JsonProperty("error")
    private String error;

    @Schema(description = "A human-readable explanation of the error.")
    @JsonProperty("error_description")
    private String errorDescription;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

}
