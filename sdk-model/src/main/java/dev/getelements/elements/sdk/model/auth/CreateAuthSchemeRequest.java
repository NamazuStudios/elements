package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.BASE_64;

/** Represents a request to create an Auth Scheme for an Application. */
@Schema(description = "Represents a request to create an Auth Scheme for an Application.")
public class CreateAuthSchemeRequest {

    /** Creates a new instance. */
    public CreateAuthSchemeRequest() {}

    @NotNull
    @Schema(description = "The JWT audience for the scheme. Must be unique.")
    private String audience;

    @Pattern(regexp = BASE_64)
    @Schema(description =
        "The Base64 public key that was either given or generated during creation. " +
        "See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/X509EncodedKeySpec.html " +
        "for details on the specifics of the format.")
    private String publicKey;

    @NotNull
    @Schema
    private PrivateKeyCrytpoAlgorithm algorithm;

    @NotNull
    @Schema(description = "The highest permitted user level this particular scheme will authorize.")
    private User.Level userLevel;

    @NotNull
    @Schema(description = "A list of tags used to index the auth scheme.")
    private List<String> tags;

    @NotNull
    @Schema(description = "The list of issuers allowed to use this scheme.")
    private List<String> allowedIssuers;

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
     * Returns the base-64 encoded public key.
     *
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets the base-64 encoded public key.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Returns the cryptographic algorithm.
     *
     * @return the algorithm
     */
    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the cryptographic algorithm.
     *
     * @param algorithm the algorithm
     */
    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns the highest permitted user level.
     *
     * @return the user level
     */
    public User.Level getUserLevel() {
        return userLevel;
    }

    /**
     * Sets the highest permitted user level.
     *
     * @param userLevel the user level
     */
    public void setUserLevel(User.Level userLevel) {
        this.userLevel = userLevel;
    }

    /**
     * Returns the tags for this auth scheme request.
     *
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags for this auth scheme request.
     *
     * @param tags the tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns the list of allowed issuers.
     *
     * @return the allowed issuers
     */
    public List<String> getAllowedIssuers() {
        return allowedIssuers;
    }

    /**
     * Sets the list of allowed issuers.
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
        CreateAuthSchemeRequest that = (CreateAuthSchemeRequest) o;
        return Objects.equals(getAudience(), that.getAudience()) && Objects.equals(getPublicKey(), that.getPublicKey()) && getAlgorithm() == that.getAlgorithm() && getUserLevel() == that.getUserLevel() && Objects.equals(getTags(), that.getTags()) && Objects.equals(getAllowedIssuers(), that.getAllowedIssuers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAudience(), getPublicKey(), getAlgorithm(), getUserLevel(), getTags(), getAllowedIssuers());
    }

}
