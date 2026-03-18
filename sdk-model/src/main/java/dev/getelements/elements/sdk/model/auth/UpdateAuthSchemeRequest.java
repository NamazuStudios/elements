package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.BASE_64;

/** Represents a request to update an Auth Scheme for an Application. */
@Schema(description = "Represents a request to update an Auth Scheme for an Application.")
public class UpdateAuthSchemeRequest {

    /** Creates a new instance. */
    public UpdateAuthSchemeRequest() {}

    /** The JWT audience for the scheme. Must be unique. */
    @NotNull
    @Schema(description = "The JWT audience for the scheme. Must be unique.")
    public String audience;

    /** Whether to regenerate the key; if true, pubKey must be null or omitted. */
    @Schema(description = "If set to true, Elements will regenerate the key and pubKey must be null or omitted")
    public boolean regenerate;

    @Pattern(regexp = BASE_64)
    @Schema(description =
        "The Base64 public key that was either given or generated during creation. " +
        "See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/X509EncodedKeySpec.html " +
        "for details on the specifics of the format.")
    private String publicKey;

    @NotNull
    @Schema(description = "The algorithm that Elements will use with the supplied key.")
    private PrivateKeyCrytpoAlgorithm algorithm;

    /** The highest permitted user level this particular scheme will authorize. */
    @NotNull
    @Schema(description = "The highest permitted user level this particular scheme will authorize.")
    public User.Level userLevel;

    /** The list of tags for tagging the auth scheme. */
    @NotNull
    @Schema(description = "The list of tags for tagging the auth scheme.")
    public List<String> tags;

    /** The list of issuers allowed to use this scheme. */
    @NotNull
    @Schema(description = "The list of issuers allowed to use this scheme.")
    public List<String> allowedIssuers;

    /**
     * Returns the JWT audience for the scheme.
     *
     * @return the audience
     */
    public String getAudience() {
        return audience;
    }

    /**
     * Sets the JWT audience for the scheme.
     *
     * @param audience the audience
     */
    public void setAudience(String audience) {
        this.audience = audience;
    }

    /**
     * Returns whether the key should be regenerated.
     *
     * @return true if the key should be regenerated
     */
    public boolean isRegenerate() {
        return regenerate;
    }

    /**
     * Sets whether the key should be regenerated.
     *
     * @param regenerate true if the key should be regenerated
     */
    public void setRegenerate(boolean regenerate) {
        this.regenerate = regenerate;
    }

    /**
     * Returns the Base64-encoded public key.
     *
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets the Base64-encoded public key.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Returns the cryptographic algorithm to use with the supplied key.
     *
     * @return the algorithm
     */
    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the cryptographic algorithm to use with the supplied key.
     *
     * @param algorithm the algorithm
     */
    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns the highest permitted user level this scheme will authorize.
     *
     * @return the user level
     */
    public User.Level getUserLevel() {
        return userLevel;
    }

    /**
     * Sets the highest permitted user level this scheme will authorize.
     *
     * @param userLevel the user level
     */
    public void setUserLevel(User.Level userLevel) {
        this.userLevel = userLevel;
    }

    /**
     * Returns the tags for the auth scheme.
     *
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags for the auth scheme.
     *
     * @param tags the tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns the list of issuers allowed to use this scheme.
     *
     * @return the allowed issuers
     */
    public List<String> getAllowedIssuers() {
        return allowedIssuers;
    }

    /**
     * Sets the list of issuers allowed to use this scheme.
     *
     * @param allowedIssuers the allowed issuers
     */
    public void setAllowedIssuers(List<String> allowedIssuers) {
        this.allowedIssuers = allowedIssuers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateAuthSchemeRequest that = (UpdateAuthSchemeRequest) o;
        return isRegenerate() == that.isRegenerate() && Objects.equals(getAudience(), that.getAudience()) && Objects.equals(getPublicKey(), that.getPublicKey()) && getAlgorithm() == that.getAlgorithm() && getUserLevel() == that.getUserLevel() && Objects.equals(getTags(), that.getTags()) && Objects.equals(getAllowedIssuers(), that.getAllowedIssuers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAudience(), isRegenerate(), getPublicKey(), getAlgorithm(), getUserLevel(), getTags(), getAllowedIssuers());
    }
}
