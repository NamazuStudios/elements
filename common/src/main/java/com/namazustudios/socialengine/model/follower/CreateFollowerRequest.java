package com.namazustudios.socialengine.model.follower;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Represents a player's Follower.  This includes the Id's of both the logged in profile, and the followed profile")
public class CreateFollowerRequest {

    @ApiModelProperty("The profile id which to follow.")
    @NotNull
    private String followedId;

    public String getFollowedId() {
        return followedId;
    }

    public void setFollowedId(String followedId) {
        this.followedId = followedId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateFollowerRequest)) return false;

        CreateFollowerRequest createFollowerRequest = (CreateFollowerRequest) o;

        return getFollowedId() != null ? getFollowedId().equals(createFollowerRequest.getFollowedId()) : createFollowerRequest.getFollowedId() == null;
    }

    @Override
    public int hashCode() {
        return getFollowedId() != null ? getFollowedId().hashCode() : 0;
    }

}
