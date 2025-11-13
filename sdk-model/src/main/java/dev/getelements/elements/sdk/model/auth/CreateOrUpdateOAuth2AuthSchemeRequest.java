package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Objects;

@Schema(description = "Represents a request to update an Auth Scheme for an Application.")
public class CreateOrUpdateOAuth2AuthSchemeRequest {

    @NotNull(groups = ValidationGroups.Insert.class)
    @Schema(description = "A unique name used to identify the scheme within the instance of Elements. " +
            "If using the same OAuth2 provider (e.g. Steam), it is recommended to suffix the name for each application " +
            "when using multitenancy, e.g. steam_game1, steam_game2, etc.")
    private String name;

    @NotNull
    @Schema(description = "The URL to send the user token validation request to.")
    private String validationUrl;

    @Schema(description = "The headers required for the validation request.")
    private List<OAuth2RequestKeyValue> headers;

    @Schema(description = "The query parameters required for the validation request.")
    private List<OAuth2RequestKeyValue> params;

    @Schema(description = "Determines how to map the user id in the response. For example \"response.params.steamid\"")
    private String responseIdMapping;

    public String getName() {
        return name;
    }

    public void setName(String issuer) {
        this.name = issuer;
    }

    public String getValidationUrl() {
        return validationUrl;
    }

    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
    }

    public List<OAuth2RequestKeyValue> getHeaders() {
        return headers;
    }

    public void setHeaders(List<OAuth2RequestKeyValue> headers) {
        this.headers = headers;
    }

    public List<OAuth2RequestKeyValue> getParams() {
        return params;
    }

    public void setParams(List<OAuth2RequestKeyValue> params) {
        this.params = params;
    }

    public String getResponseIdMapping() {
        return responseIdMapping;
    }

    public void setResponseIdMapping(String responseIdMapping) {
        this.responseIdMapping = responseIdMapping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuth2AuthScheme that = (OAuth2AuthScheme) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getHeaders(), that.getHeaders()) && Objects.equals(getParams(), that.getParams()) && Objects.equals(getValidationUrl(), that.getValidationUrl()) && Objects.equals(getResponseIdMapping(), that.getResponseIdMapping());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValidationUrl(), getHeaders(), getParams(), getResponseIdMapping());
    }
}
