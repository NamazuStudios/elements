package com.namazustudios.socialengine.model.session;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;

/**
 * Represents a session authorized by Facebook.  This includes
 *
 * Created by patricktwohig on 6/22/17.
 */
@ApiModel
public class FacebookSession {

    private User user;

    private Profile profile;

    private Application application;

    private String userAccessToken;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacebookSession)) return false;

        FacebookSession that = (FacebookSession) o;

        if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) return false;
        if (getProfile() != null ? !getProfile().equals(that.getProfile()) : that.getProfile() != null) return false;
        if (getApplication() != null ? !getApplication().equals(that.getApplication()) : that.getApplication() != null)
            return false;
        return getUserAccessToken() != null ? getUserAccessToken().equals(that.getUserAccessToken()) : that.getUserAccessToken() == null;
    }

    @Override
    public int hashCode() {
        int result = getUser() != null ? getUser().hashCode() : 0;
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        result = 31 * result + (getApplication() != null ? getApplication().hashCode() : 0);
        result = 31 * result + (getUserAccessToken() != null ? getUserAccessToken().hashCode() : 0);
        return result;
    }

}
