package dev.getelements.elements.sdk.model.follower;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "Represents a request to follow a player Follower.")
public class CreateFollowerRequest {

    @Schema(description = "The profile id which to follow.")
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
