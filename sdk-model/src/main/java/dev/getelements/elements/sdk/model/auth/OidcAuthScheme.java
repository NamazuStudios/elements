package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;


public class OidcAuthScheme {

    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The unique ID of the auth scheme.")
    private String id;

    @Schema(description = "The unique name of the auth scheme.")
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OidcAuthScheme that = (OidcAuthScheme) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getKeys(), that.getKeys()) && Objects.equals(getIssuer(), that.getIssuer()) && Objects.equals(getKeysUrl(), that.getKeysUrl()) && Objects.equals(getMediaType(), that.getMediaType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getIssuer(), getKeys(), getKeysUrl(), getMediaType());
    }

    @Override
    public String toString() {
        return "OidcAuthScheme{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", issuer='" + issuer + '\'' +
                ", keys='" + keys + '\'' +
                ", keysUrl='" + keysUrl + '\'' +
                ", mediaType='" + mediaType +
                '}';
    }
}
