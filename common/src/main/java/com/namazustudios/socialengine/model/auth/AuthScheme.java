package com.namazustudios.socialengine.model.auth;

import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static com.namazustudios.socialengine.Constants.Regexp.BASE_64;

@ApiModel
public class AuthScheme implements Serializable {

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The unique ID of the auth scheme.")
    private String id;

    @NotNull
    @ApiModelProperty("A unique name used to identify the scheme within the instance of Elements.")
    private String audience;

    @NotNull
    @Pattern(regexp = BASE_64)
    @ApiModelProperty("A base-64 encoded string representing an x509 encoded public key.")
    private String publicKey;

    @NotNull
    @ApiModelProperty("The digital signature matching the public key format.")
    private PrivateKeyCrytpoAlgorithm algorithm;

    @NotNull
    @ApiModelProperty("The highest permitted user level this particular scheme will authorize.")
    private User.Level userLevel;

    @NotNull
    @ApiModelProperty("The tags used to tag this auth scheme.")
    private List<String> tags;

    @NotNull
    @ApiModelProperty("A list of issuers allowed to use this scheme.")
    private List<String> allowedIssuers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        AuthScheme that = (AuthScheme) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getAudience(), that.getAudience()) && Objects.equals(getPublicKey(), that.getPublicKey()) && getAlgorithm() == that.getAlgorithm() && getUserLevel() == that.getUserLevel() && Objects.equals(getTags(), that.getTags()) && Objects.equals(getAllowedIssuers(), that.getAllowedIssuers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAudience(), getPublicKey(), getAlgorithm(), getUserLevel(), getTags(), getAllowedIssuers());
    }

}
