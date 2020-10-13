package com.namazustudios.socialengine.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class AppleSignInSessionRequest {

    @NotNull
    @ApiModelProperty
    private String applicationNameOrId;

    @NotNull
    @ApiModelProperty
    private String applicationConfigurationNameOrId;

    @NotNull
    @ApiModelProperty
    private String authCode;

    @NotNull
    @ApiModelProperty
    private String identityToken;

    public String getApplicationNameOrId() {
        return applicationNameOrId;
    }

    public void setApplicationNameOrId(String applicationNameOrId) {
        this.applicationNameOrId = applicationNameOrId;
    }

    public String getApplicationConfigurationNameOrId() {
        return applicationConfigurationNameOrId;
    }

    public void setApplicationConfigurationNameOrId(String applicationConfigurationNameOrId) {
        this.applicationConfigurationNameOrId = applicationConfigurationNameOrId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getIdentityToken() {
        return identityToken;
    }

    public void setIdentityToken(String identityToken) {
        this.identityToken = identityToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppleSignInSessionRequest that = (AppleSignInSessionRequest) o;
        return Objects.equals(getApplicationNameOrId(), that.getApplicationNameOrId()) &&
                Objects.equals(getApplicationConfigurationNameOrId(), that.getApplicationConfigurationNameOrId()) &&
                Objects.equals(getAuthCode(), that.getAuthCode()) &&
                Objects.equals(getIdentityToken(), that.getIdentityToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApplicationNameOrId(), getApplicationConfigurationNameOrId(), getAuthCode(), getIdentityToken());
    }

}
