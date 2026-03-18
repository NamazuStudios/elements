package dev.getelements.elements.sdk.model.user;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to update a user's password. */
@Schema
public class UserUpdatePasswordRequest {

    /** Creates a new instance. */
    public UserUpdatePasswordRequest() {}

    @NotNull
    @Schema(description = "The user's current password.")
    private String oldPassword;

    @NotNull
    @Schema(description = "The user's updated password.")
    private String newPassword;

    @Schema(description = "The user's profile id to assign the new session. Leave blank for no profile id.")
    private String profileId;

    /**
     * Returns the user's current password.
     *
     * @return the old password
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * Sets the user's current password.
     *
     * @param oldPassword the old password
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * Returns the user's new password.
     *
     * @return the new password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Sets the user's new password.
     *
     * @param newPassword the new password
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * Returns the profile ID to assign to the new session after the password update.
     *
     * @return the profile ID
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Sets the profile ID to assign to the new session after the password update.
     *
     * @param profileId the profile ID
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdatePasswordRequest that = (UserUpdatePasswordRequest) o;
        return Objects.equals(getOldPassword(), that.getOldPassword()) && Objects.equals(getNewPassword(), that.getNewPassword()) && Objects.equals(getProfileId(), that.getProfileId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOldPassword(), getNewPassword(), getProfileId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserUpdatePasswordRequest{");
        sb.append("oldPassword='").append(oldPassword).append('\'');
        sb.append(", newPassword='").append(newPassword).append('\'');
        sb.append(", profileId='").append(profileId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
