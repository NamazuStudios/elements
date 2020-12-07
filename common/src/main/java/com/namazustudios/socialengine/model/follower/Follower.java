package com.namazustudios.socialengine.model.follower;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Represents a player's Follower.  This includes the Id's of both the logged in profile, and the followed profile")
public class Follower {

    @ApiModelProperty("The unique ID of the follow relationship.")
    private String id;

    @ApiModelProperty("The profile id associated with this particular relationship.")
    private String profileId;

    @ApiModelProperty("The profile id which is followed and associated with the relationship.")
    private String followedId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getFollowedId() {
        return followedId;
    }

    public void setFollowedId(String followedId) {
        this.followedId = followedId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Follower)) return false;

        Follower follower = (Follower) o;

        if (getId() != null ? !getId().equals(follower.getId()) : follower.getId() != null) return false;
        if (getProfileId() != null ? !getProfileId().equals(follower.getProfileId()) : follower.getProfileId() != null) return false;
        return getFollowedId() != null ? getFollowedId().equals(follower.getFollowedId()) : follower.getFollowedId() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getProfileId() != null ? getProfileId().hashCode() : 0);
        result = 31 * result + (getFollowedId() != null ? getFollowedId().hashCode() : 0);
        return result;
    }

}
