package com.namazustudios.socialengine.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a {@link Session} started from the usage of a Facebook OAuth token.  Extends {@link SessionCreation}
 * but includes extra data pertaining specifically to Facebook.
 */
@ApiModel(description = "Represents a response from the Facebook authentication service.")
public class FacebookSessionCreation extends SessionCreation {

    @ApiModelProperty("The Facebook user access token obtained via the Facebook API.")
    private String userAccessToken;

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

}
