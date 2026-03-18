package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.BASE_64;

/** Represents an authentication scheme used to validate JWT tokens. */
@Schema
public class AuthScheme implements Serializable {

    /** Creates a new instance. */
    public AuthScheme() {}

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The unique ID of the auth scheme.")
    private String id;

    @NotNull
    @Schema(description = "A unique name used to identify the scheme within the instance of Elements.")
    private String audience;

    @NotNull
    @Pattern(regexp = BASE_64)
    @Schema(description = "A base-64 encoded string representing an x509 encoded public key.")
    private String publicKey;

    @NotNull
    @Schema(description = "The digital signature matching the public key format.")
    private PrivateKeyCrytpoAlgorithm algorithm;

    @NotNull
    @Schema(description = "The highest permitted user level this particular scheme will authorize.")
    private User.Level userLevel;

    @NotNull
    @Schema(description = "The tags used to tag this auth scheme.")
    private List<String> tags;

    @NotNull
    @Schema(description = "A list of issuers allowed to use this scheme.")
    private List<String> allowedIssuers;

    /**
     * Returns the unique ID of the auth scheme.
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the auth scheme.
     * @param id the ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the audience name for this scheme.
     * @return the audience
     */
    public String getAudience() {
        return audience;
    }

    /**
     * Sets the audience name for this scheme.
     * @param audience the audience
     */
    public void setAudience(String audience) {
        this.audience = audience;
    }

    /**
     * Returns the base-64 encoded public key.
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets the base-64 encoded public key.
     * @param publicKey the public key
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Returns the cryptographic algorithm.
     * @return the algorithm
     */
    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the cryptographic algorithm.
     * @param algorithm the algorithm
     */
    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns the highest permitted user level.
     * @return the user level
     */
    public User.Level getUserLevel() {
        return userLevel;
    }

    /**
     * Sets the highest permitted user level.
     * @param userLevel the user level
     */
    public void setUserLevel(User.Level userLevel) {
        this.userLevel = userLevel;
    }

    /**
     * Returns the tags for this auth scheme.
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags for this auth scheme.
     * @param tags the tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns the list of allowed issuers.
     * @return the allowed issuers
     */
    public List<String> getAllowedIssuers() {
        return allowedIssuers;
    }

    /**
     * Sets the list of allowed issuers.
     * @param allowedIssuers the allowed issuers
     */
    public void setAllowedIssuers(List<String> allowedIssuers) {
        this.allowedIssuers = allowedIssuers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthScheme that = (AuthScheme) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getAudience(), that.getAudience()) && Objects.equals(getPublicKey(), that.getPublicKey()) && getAlgorithm() == that.getAlgorithm() && getUserLevel() == that.getUserLevel() && Objects.equals(getTags(), that.getTags()) && Objects.equals(getAllowedIssuers(), that.getAllowedIssuers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAudience(), getPublicKey(), getAlgorithm(), getUserLevel(), getTags(), getAllowedIssuers());
    }

}
