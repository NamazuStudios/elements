package dev.getelements.elements.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

import javax.validation.constraints.NotNull;

@ApiModel
public class FacebookSessionRequest {

    @NotNull
    @ApiModelProperty
    private String applicationNameOrId;

    @NotNull
    @ApiModelProperty
    private String applicationConfigurationNameOrId;

    @NotNull
    @ApiModelProperty
    private String facebookOAuthAccessToken;

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

    public String getFacebookOAuthAccessToken() {
        return facebookOAuthAccessToken;
    }

    public void setFacebookOAuthAccessToken(String facebookOAuthAccessToken) {
        this.facebookOAuthAccessToken = facebookOAuthAccessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacebookSessionRequest)) return false;

        FacebookSessionRequest that = (FacebookSessionRequest) o;

        if (getApplicationNameOrId() != null ? !getApplicationNameOrId().equals(that.getApplicationNameOrId()) : that.getApplicationNameOrId() != null)
            return false;
        if (getApplicationConfigurationNameOrId() != null ? !getApplicationConfigurationNameOrId().equals(that.getApplicationConfigurationNameOrId()) : that.getApplicationConfigurationNameOrId() != null)
            return false;
        return getFacebookOAuthAccessToken() != null ? getFacebookOAuthAccessToken().equals(that.getFacebookOAuthAccessToken()) : that.getFacebookOAuthAccessToken() == null;
    }

    @Override
    public int hashCode() {
        int result = getApplicationNameOrId() != null ? getApplicationNameOrId().hashCode() : 0;
        result = 31 * result + (getApplicationConfigurationNameOrId() != null ? getApplicationConfigurationNameOrId().hashCode() : 0);
        result = 31 * result + (getFacebookOAuthAccessToken() != null ? getFacebookOAuthAccessToken().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FacebookSessionRequest{" +
                "applicationNameOrId='" + applicationNameOrId + '\'' +
                ", applicationConfigurationNameOrId='" + applicationConfigurationNameOrId + '\'' +
                ", facebookOAuthAccessToken='" + facebookOAuthAccessToken + '\'' +
                '}';
    }

}
