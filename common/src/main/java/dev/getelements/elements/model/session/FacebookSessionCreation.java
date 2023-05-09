package dev.getelements.elements.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FacebookSessionCreation that = (FacebookSessionCreation) o;
        return Objects.equals(getUserAccessToken(), that.getUserAccessToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getUserAccessToken());
    }

}
