package com.namazustudios.socialengine.model.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "Represents a request to update an Auth Scheme for an Application.")
public class UpdateAuthSchemeRequest {

    @ApiModelProperty("The unique id of the auth scheme.")
    @NotNull
    public String authSchemeId;

    @ApiModelProperty("The JWT audience for the scheme. Must be unique.")
    @NotNull
    public String aud;

    @ApiModelProperty("If set to true, Elements will regenerate the key and pubKey must be null or omitted")
    @NotNull
    public Boolean regenerate;

    @ApiModelProperty("The public key for the scheme. If null, Elements will generate a public and private key pair with the response.")
    public String pubKey;

    @ApiModelProperty("The highest permitted user level this particular scheme will authorize.")
    @NotNull
    public String userLevel;

    @ApiModelProperty("The list of issuers allowed to use this scheme.")
    @NotNull
    public List<String> allowedIssuers;

    public String getAuthSchemeId() {
        return authSchemeId;
    }

    public void setAuthSchemeId(String authSchemeId) {
        this.authSchemeId = authSchemeId;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public Boolean getRegenerate() { return regenerate; }

    public void setRegenerate(Boolean regenerate) { this.regenerate = regenerate; }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public List<String> getAllowedIssuers() {
        return allowedIssuers;
    }

    public void setAllowedIssuers(List<String> allowedIssuers) {
        this.allowedIssuers = allowedIssuers;
    }
}
