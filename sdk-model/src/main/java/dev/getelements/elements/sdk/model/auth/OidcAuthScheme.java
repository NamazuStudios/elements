package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;


/** Represents an OpenID Connect (OIDC) authentication scheme used to validate JWT tokens. */
public class OidcAuthScheme {

    /** Creates a new instance. */
    public OidcAuthScheme() {}

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

    /**
     * Returns the unique ID of the auth scheme.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the auth scheme.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the unique name of the auth scheme.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the auth scheme.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the issuer identifier, which must match the 'iss' property of the decoded JWT.
     *
     * @return the issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Sets the issuer identifier.
     *
     * @param issuer the issuer
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Returns the JWKs used to validate JWT signatures.
     *
     * @return the keys
     */
    public List<JWK> getKeys() {
        return keys;
    }

    /**
     * Sets the JWKs used to validate JWT signatures.
     *
     * @param keys the keys
     */
    public void setKeys(List<JWK> keys) {
        this.keys = keys;
    }

    /**
     * Returns the URL for the JWK data.
     *
     * @return the keys URL
     */
    public String getKeysUrl() {
        return keysUrl;
    }

    /**
     * Sets the URL for the JWK data.
     *
     * @param keysUrl the keys URL
     */
    public void setKeysUrl(String keysUrl) {
        this.keysUrl = keysUrl;
    }

    /**
     * Returns the media type for the JWK format.
     *
     * @return the media type
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Sets the media type for the JWK format.
     *
     * @param mediaType the media type
     */
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
