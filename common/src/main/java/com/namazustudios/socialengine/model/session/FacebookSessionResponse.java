package com.namazustudios.socialengine.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a {@link Session} started from the usage of a Facebook OAuth token.
 */
@ApiModel(description = "Represents a response from the Facebook authentication service.")
public class FacebookSessionResponse {

    @ApiModelProperty("The Session that was created with the Facebook credentials")
    private Session session;

    @ApiModelProperty("The Facebook user access token obtained via the Facebook API.")
    private String userAccessToken;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

}
