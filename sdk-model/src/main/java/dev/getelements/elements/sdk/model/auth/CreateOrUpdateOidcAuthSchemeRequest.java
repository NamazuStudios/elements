package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Objects;

@Schema(description = "Represents a request to update an Auth Scheme for an Application.")
public class CreateOrUpdateOidcAuthSchemeRequest {

    @NotNull
    @Schema(description = "The name used when linking the scheme to the user.")
    private String name;

    @NotNull
    @Schema(description = "A unique name used to identify the scheme within the instance of Elements. " +
            "When validating from an external source (e.g. Google or Apple SSO), must match the 'iss' property of the decoded JWT.")
    private String issuer;

    @NotNull
    @Schema(description = "A set of JWKs containing the keys required to validate JWT signatures.")
    private List<JWK> keys;

    @Schema(description = "The URL for the JWK data. Will attempt to refresh keys if the kid cannot be found in the collection.")
    private String keysUrl;

    @Schema(description = "The JWK format. Defaults to application/json")
    private String mediaType = "application/json";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public List<JWK> getKeys() {
        return keys;
    }

    public void setKeys(List<JWK> keys) {
        this.keys = keys;
    }

    public String getKeysUrl() {
        return keysUrl;
    }

    public void setKeysUrl(String keysUrl) {
        this.keysUrl = keysUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateOrUpdateOidcAuthSchemeRequest that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(issuer, that.issuer) && Objects.equals(keys, that.keys) && Objects.equals(keysUrl, that.keysUrl) && Objects.equals(mediaType, that.mediaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, issuer, keys, keysUrl, mediaType);
    }

    @Override
    public String toString() {
        return "CreateOrUpdateOidcAuthSchemeRequest{" +
                "name='" + name + '\'' +
                ", issuer='" + issuer + '\'' +
                ", keys=" + keys +
                ", keysUrl='" + keysUrl + '\'' +
                ", mediaType='" + mediaType + '\'' +
                '}';
    }
}
