package com.namazustudios.socialengine.model.auth;

import com.namazustudios.socialengine.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;

import static com.namazustudios.socialengine.Constants.Regexp.BASE_64;

@ApiModel(description = "Represents a request to update an Auth Scheme for an Application.")
public class UpdateAuthSchemeRequest {

    @NotNull
    @ApiModelProperty("The JWT audience for the scheme. Must be unique.")
    public String audience;

    @ApiModelProperty("If set to true, Elements will regenerate the key and pubKey must be null or omitted")
    public boolean regenerate;

    @Pattern(regexp = BASE_64)
    @ApiModelProperty("The public key for the scheme. If null, Elements will ignore this field and regenerate the key " +
                      "if the regenerate field is set to true.")
    public String pubKey;

    @NotNull
    @ApiModelProperty("The algorithm that Elements will use with the supplied key.")
    private AuthSchemeAlgorithm algorithm;

    @NotNull
    @ApiModelProperty("The highest permitted user level this particular scheme will authorize.")
    public User.Level userLevel;

    @NotNull
    @ApiModelProperty("The list of issuers allowed to use this scheme.")
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

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public AuthSchemeAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AuthSchemeAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public User.Level getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(User.Level userLevel) {
        this.userLevel = userLevel;
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
        return isRegenerate() == that.isRegenerate() && Objects.equals(getAudience(), that.getAudience()) && Objects.equals(getPubKey(), that.getPubKey()) && getAlgorithm() == that.getAlgorithm() && Objects.equals(getUserLevel(), that.getUserLevel()) && Objects.equals(getAllowedIssuers(), that.getAllowedIssuers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAudience(), isRegenerate(), getPubKey(), getAlgorithm(), getUserLevel(), getAllowedIssuers());
    }

}
