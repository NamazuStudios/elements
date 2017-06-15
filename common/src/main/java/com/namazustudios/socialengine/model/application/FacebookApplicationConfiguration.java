package com.namazustudios.socialengine.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 *
 * Created by patricktwohig on 6/14/17.
 */
@ApiModel(
    value = "The Facebook Application Configuration",
    description = "Houses the various parameters required which allow communication with " +
                  "the Faceook API.  The Facebook API will ")
public class FacebookApplicationConfiguration extends ApplicationConfiguration {

    @ApiModelProperty("The AppID as it appears in the Facebook Developer Console")
    private String applicationId;

    @ApiModelProperty("The App Secret as it appears in the Facebook Developer Console")
    private String applicationSecret;

    @ApiModelProperty("The set of built-in permissions connected clients will need to request.")
    private List<String> builtinApplicationPermissions;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public List<String> getBuiltinApplicationPermissions() {
        return builtinApplicationPermissions;
    }

    public void setBuiltinApplicationPermissions(List<String> builtinApplicationPermissions) {
        this.builtinApplicationPermissions = builtinApplicationPermissions;
    }

}
