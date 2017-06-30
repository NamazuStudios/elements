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

    private String appSecretProof;

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

    public String getAppSecretProof() {
        return appSecretProof;
    }

    public void setAppSecretProof(String appSecretProof) {
        this.appSecretProof = appSecretProof;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacebookSession)) return false;

        FacebookSession that = (FacebookSession) o;

        if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) return false;
        if (getLongLivedToken() != null ? !getLongLivedToken().equals(that.getLongLivedToken()) : that.getLongLivedToken() != null)
            return false;
        return getAppSecretProof() != null ? getAppSecretProof().equals(that.getAppSecretProof()) : that.getAppSecretProof() == null;
    }

    @Override
    public int hashCode() {
        int result = getUser() != null ? getUser().hashCode() : 0;
        result = 31 * result + (getLongLivedToken() != null ? getLongLivedToken().hashCode() : 0);
        result = 31 * result + (getAppSecretProof() != null ? getAppSecretProof().hashCode() : 0);
        return result;
    }
}
