package com.namazustudios.socialengine.model.session;

import com.namazustudios.socialengine.model.User;
import io.swagger.annotations.ApiModel;

/**
 * Represents a session authorized by Facebook.  This includes
 *
 * Created by patricktwohig on 6/22/17.
 */
@ApiModel
public class FacebookSession {

    private User user;

    private String longLivedToken;

    private String applicationName;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLongLivedToken() {
        return longLivedToken;
    }

    public void setLongLivedToken(String longLivedToken) {
        this.longLivedToken = longLivedToken;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
