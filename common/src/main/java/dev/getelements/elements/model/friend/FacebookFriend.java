package dev.getelements.elements.model.friend;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class FacebookFriend {

    @ApiModelProperty("The Facebook-assigned ID")
    public String facebookId;

    @ApiModelProperty("The display name of the friend.")
    public String displayName;

    @ApiModelProperty("The profile picture of the friend.")
    public String profilePictureUrl;

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacebookFriend)) return false;

        FacebookFriend that = (FacebookFriend) o;

        if (getFacebookId() != null ? !getFacebookId().equals(that.getFacebookId()) : that.getFacebookId() != null)
            return false;
        if (getDisplayName() != null ? !getDisplayName().equals(that.getDisplayName()) : that.getDisplayName() != null)
            return false;
        return getProfilePictureUrl() != null ? getProfilePictureUrl().equals(that.getProfilePictureUrl()) : that.getProfilePictureUrl() == null;
    }

    @Override
    public int hashCode() {
        int result = getFacebookId() != null ? getFacebookId().hashCode() : 0;
        result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
        result = 31 * result + (getProfilePictureUrl() != null ? getProfilePictureUrl().hashCode() : 0);
        return result;
    }

}
