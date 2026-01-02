package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;
import java.util.Objects;

public class OAuth2SessionRequest {

    @NotBlank
    @Schema(description = "The OAuth2 scheme ID to use.")
    private String schemeId;

    @Schema(description = "The request parameters to be used in the token validation request. " +
            "These will automatically be mapped to the corresponding query param/header/body value defined in the auth scheme.")
    private Map<String, String> requestParameters;

    @Schema(description = """
            The request headers to be used in the token validation request. This should adhere to any headers marked as fromClient in the auth scheme.\
            Deprecated: All key/values can now be placed in the requestParameters and they will be mapped automatically.
            Internally, this is combined with requestParameters for backwards compatibility.""")
    @Deprecated
    private Map<String, String> requestHeaders;

    @Schema(description = "The profile ID to assign to the session.")
    private String profileId;

    @Schema(description = "A query string to select the profile to use. " +
            "NOTE: This will not be run if a profileId is specified.")
    private String profileSelector;

    public Map<String, String> getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(Map<String, String> requestParameters) {
        this.requestParameters = requestParameters;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileSelector() {
        return profileSelector;
    }

    public void setProfileSelector(String profileSelector) {
        this.profileSelector = profileSelector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuth2SessionRequest that = (OAuth2SessionRequest) o;
        return Objects.equals(getRequestParameters(), that.getRequestParameters()) && Objects.equals(getRequestHeaders(), that.getRequestHeaders()) && Objects.equals(getSchemeId(), that.getSchemeId()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getProfileSelector(), that.getProfileSelector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequestParameters(), getRequestHeaders(), getSchemeId(), getProfileId(), getProfileSelector());
    }

    @Override
    public String toString() {
        return "OAuth2SessionRequest{" +
                "requestParameters='" + requestParameters + '\'' +
                "requestHeaders='" + requestHeaders + '\'' +
                "schemeId='" + schemeId + '\'' +
                ", profileId='" + profileId + '\'' +
                ", profileSelector='" + profileSelector + '\'' +
                '}';
    }

}
