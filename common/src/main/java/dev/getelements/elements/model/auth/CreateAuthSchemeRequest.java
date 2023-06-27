package dev.getelements.elements.model.auth;

import dev.getelements.elements.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.Constants.Regexp.BASE_64;

@ApiModel(description = "Represents a request to create an Auth Scheme for an Application.")
public class CreateAuthSchemeRequest {

    @NotNull
    @ApiModelProperty("The JWT audience for the scheme. Must be unique.")
    private String audience;

    @Pattern(regexp = BASE_64)
    @ApiModelProperty(
        "The Base64 public key that was either given or generated during creation. " +
        "See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/X509EncodedKeySpec.html " +
        "for details on the specifics of the format.")
    private String publicKey;

    @NotNull
    @ApiModelProperty
    private PrivateKeyCrytpoAlgorithm algorithm;

    @NotNull
    @ApiModelProperty("The highest permitted user level this particular scheme will authorize.")
    private User.Level userLevel;

    @NotNull
    @ApiModelProperty("A list of tags used to index the auth scheme.")
    private List<String> tags;

    @NotNull
    @ApiModelProperty("The list of issuers allowed to use this scheme.")
    private List<String> allowedIssuers;

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
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
        CreateAuthSchemeRequest that = (CreateAuthSchemeRequest) o;
        return Objects.equals(getAudience(), that.getAudience()) && Objects.equals(getPublicKey(), that.getPublicKey()) && getAlgorithm() == that.getAlgorithm() && getUserLevel() == that.getUserLevel() && Objects.equals(getTags(), that.getTags()) && Objects.equals(getAllowedIssuers(), that.getAllowedIssuers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAudience(), getPublicKey(), getAlgorithm(), getUserLevel(), getTags(), getAllowedIssuers());
    }

}
