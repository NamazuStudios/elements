package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@ApiModel
public class SubjectRequest {

    @ApiModelProperty("Flag to check who may perform the operation. If true, all anonymous users may perform the operation.")
    private boolean wildcard;

    @NotNull
    @ApiModelProperty("A List of all UserIds which can operate against the LargeObject.")
    private List<String> userIds;

    @NotNull
    @ApiModelProperty("A List of all ProfileIds which can operate against the LargeObject.")
    private List<String> profileIds;

    /**
     * Gets a default valid {@link SubjectRequest}.
     * @return
     */
    public static SubjectRequest newDefaultRequest() {
        final var request = new SubjectRequest();
        request.setWildcard(false);
        request.setUserIds(List.of());
        request.setProfileIds(List.of());
        return request;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public List<String> getProfileIds() {
        return profileIds;
    }

    public void setProfileIds(List<String> profileIds) {
        this.profileIds = profileIds;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectRequest that = (SubjectRequest) o;
        return isWildcard() == that.isWildcard() && Objects.equals(getUserIds(), that.getUserIds()) && Objects.equals(getProfileIds(), that.getProfileIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isWildcard(), getUserIds(), getProfileIds());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SubjectRequest{");
        sb.append("allUsers=").append(wildcard);
        sb.append(", userIds=").append(userIds);
        sb.append(", profileIds=").append(profileIds);
        sb.append('}');
        return sb.toString();
    }

}
