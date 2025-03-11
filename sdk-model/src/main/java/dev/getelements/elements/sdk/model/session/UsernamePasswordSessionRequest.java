package dev.getelements.elements.sdk.model.session;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Schema
public class UsernamePasswordSessionRequest {

    @NotBlank
    @Schema(description = "The user ID.")
    private String userId;

    @NotBlank
    @Schema(description = "The password.")
    private String password;

    @Schema(description = "The profile ID to assign to the session.")
    private String profileId;

    @Schema(description = "A query string to select the profile to use.")
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
