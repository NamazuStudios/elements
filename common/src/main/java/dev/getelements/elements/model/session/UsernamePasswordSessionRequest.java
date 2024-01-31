package dev.getelements.elements.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@ApiModel
public class UsernamePasswordSessionRequest {

    @NotBlank
    @ApiModelProperty("The user ID.")
    private String userId;

    @NotBlank
    @ApiModelProperty("The password.")
    private String password;

    @ApiModelProperty("The profile ID to assign to the session.")
    private String profileId;

    @ApiModelProperty("A query string to select the profile to use.")
    private String profileSelector;

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

    public String getProfileSelector() {
        return profileSelector;
    }

    public void setProfileSelector(String profileSelector) {
        this.profileSelector = profileSelector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsernamePasswordSessionRequest that = (UsernamePasswordSessionRequest) o;
        return Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getPassword(), that.getPassword()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getProfileSelector(), that.getProfileSelector());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getPassword(), getProfileId(), getProfileSelector());
    }

    @Override
    public String toString() {
        return "UsernamePasswordSessionRequest{" +
                "userId='" + userId + '\'' +
                ", password='...you keep your secrets" + '\'' +
                ", profileId='" + profileId + '\'' +
                ", profileSelector='" + profileSelector + '\'' +
                '}';
    }

}
