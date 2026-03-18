package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Objects;

/** Represents a request to create or update an OIDC Auth Scheme for an Application. */
@Schema(description = "Represents a request to update an Auth Scheme for an Application.")
public class CreateOrUpdateOidcAuthSchemeRequest {

    /** Creates a new instance. */
    public CreateOrUpdateOidcAuthSchemeRequest() {}

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
     * Returns the issuer identifier.
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
     * Returns the set of JWKs for JWT signature validation.
     *
     * @return the keys
     */
    public List<JWK> getKeys() {
        return keys;
    }

    /**
     * Sets the set of JWKs for JWT signature validation.
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
        CreateOrUpdateOidcAuthSchemeRequest that = (CreateOrUpdateOidcAuthSchemeRequest) o;
        return Objects.equals(getKeys(), that.getKeys()) && Objects.equals(getIssuer(), that.getIssuer()) && Objects.equals(getKeysUrl(), that.getKeysUrl()) && Objects.equals(getMediaType(), that.getMediaType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIssuer(), getKeys(), getKeysUrl(), getMediaType());
    }

}
