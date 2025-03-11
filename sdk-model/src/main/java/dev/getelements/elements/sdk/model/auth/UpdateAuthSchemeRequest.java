package dev.getelements.elements.sdk.model.auth;

import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.BASE_64;

@Schema(description = "Represents a request to update an Auth Scheme for an Application.")
public class UpdateAuthSchemeRequest {

    @NotNull
    @Schema(description = "The JWT audience for the scheme. Must be unique.")
    public String audience;

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

    @NotNull
    @Schema(description = "The highest permitted user level this particular scheme will authorize.")
    public User.Level userLevel;

    @NotNull
    @Schema(description = "The list of tags for tagging the auth scheme.")
    public List<String> tags;

    @NotNull
    @Schema(description = "The list of issuers allowed to use this scheme.")
    public List<String> allowedIssuers;

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public void setRegenerate(boolean regenerate) {
        this.regenerate = regenerate;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public User.Level getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(User.Level userLevel) {
        this.userLevel = userLevel;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getAllowedIssuers() {
        return allowedIssuers;
    }

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
