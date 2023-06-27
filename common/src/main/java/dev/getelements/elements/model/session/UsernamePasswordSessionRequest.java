package dev.getelements.elements.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class UsernamePasswordSessionRequest {

    @NotNull
    @ApiModelProperty("The user ID.")
    private String userId;

    @NotNull
    @ApiModelProperty("The password.")
    private String password;

    @ApiModelProperty("The profile ID to assign to the session.")
    private String profileId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UsernamePasswordSessionRequest)) return false;
        UsernamePasswordSessionRequest that = (UsernamePasswordSessionRequest) object;
        return Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getPassword(), that.getPassword()) &&
                Objects.equals(getProfileId(), that.getProfileId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getUserId(), getPassword(), getProfileId());
    }

    @Override
    public String toString() {
        return "UsernamePasswordSessionRequest{" +
                "userId='" + userId + '\'' +
                ", password='...you keep your secrets" + '\'' +
                ", profileId='" + profileId + '\'' +
                '}';
    }

}
