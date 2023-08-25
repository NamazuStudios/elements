package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@ApiModel
public class SubjectRequest {

    @ApiModelProperty("Flag to check who may perform the operations. True if all users may access the object.")
    private boolean allUsers;

    @ApiModelProperty("Flag to check who may perform the operations. True if all profiles may access the object.")
    private boolean allProfiles;

    @NotNull
    @ApiModelProperty("A List of all UserIds which can operate against the LargeObject.")
    private List<String> userIds;

    @NotNull
    @ApiModelProperty("A List of all ProfileIds which can operate against the LargeObject.")
    private List<String> profileIds;

    public boolean isAllUsers() {
        return allUsers;
    }

    public void setAllUsers(boolean allUsers) {
        this.allUsers = allUsers;
    }

    public boolean isAllProfiles() {
        return allProfiles;
    }

    public void setAllProfiles(boolean allProfiles) {
        this.allProfiles = allProfiles;
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
        return isAllUsers() == that.isAllUsers() && isAllProfiles() == that.isAllProfiles() && Objects.equals(getUserIds(), that.getUserIds()) && Objects.equals(getProfileIds(), that.getProfileIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isAllUsers(), isAllProfiles(), getUserIds(), getProfileIds());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SubjectRequest{");
        sb.append("allUsers=").append(allUsers);
        sb.append(", allProfiles=").append(allProfiles);
        sb.append(", userIds=").append(userIds);
        sb.append(", profileIds=").append(profileIds);
        sb.append('}');
        return sb.toString();
    }

}
