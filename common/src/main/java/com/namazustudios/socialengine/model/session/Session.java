package com.namazustudios.socialengine.model.session;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * Represents a session authorized by Facebook.  This includes
 *
 * Created by patricktwohig on 6/22/17.
 */
@ApiModel
public class Session implements Serializable {

    private String id;

    private User user;

    private Profile profile;

    private Application application;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        if (!(o instanceof Session)) return false;

        Session session = (Session) o;

        if (getId() != null ? !getId().equals(session.getId()) : session.getId() != null) return false;
        if (getUser() != null ? !getUser().equals(session.getUser()) : session.getUser() != null) return false;
        if (getProfile() != null ? !getProfile().equals(session.getProfile()) : session.getProfile() != null)
            return false;
        return getApplication() != null ? getApplication().equals(session.getApplication()) : session.getApplication() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        result = 31 * result + (getApplication() != null ? getApplication().hashCode() : 0);
        return result;
    }

}
