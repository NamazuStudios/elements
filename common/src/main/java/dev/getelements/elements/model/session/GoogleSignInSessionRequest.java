package dev.getelements.elements.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class GoogleSignInSessionRequest {

    @NotNull
    @ApiModelProperty
    private String applicationNameOrId;

    @NotNull
    @ApiModelProperty
    private String identityToken;

    public String getApplicationNameOrId() {
        return applicationNameOrId;
    }

    public void setApplicationNameOrId(String applicationNameOrId) {
        this.applicationNameOrId = applicationNameOrId;
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
        GoogleSignInSessionRequest that = (GoogleSignInSessionRequest) o;
        return Objects.equals(getApplicationNameOrId(), that.getApplicationNameOrId()) &&
                Objects.equals(getIdentityToken(), that.getIdentityToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApplicationNameOrId(), getIdentityToken());
    }

}
