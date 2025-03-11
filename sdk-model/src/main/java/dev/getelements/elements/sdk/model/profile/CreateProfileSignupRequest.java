package dev.getelements.elements.sdk.model.profile;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema(description = "Used in conjunction with the user creation request to specify.")
public class CreateProfileSignupRequest {

    @NotNull
    @Schema(description = "The application id this profile belongs to.")
    private String applicationId;

    @Schema(description = "A non-unique display name for this profile. If left null, the server will assign.")
    private String displayName;

    @Schema(description = "A URL to the image of the profile.  (ie the User's Avatar). If left null, the server will no assign any URL.")
    private String imageUrl;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateProfileSignupRequest that = (CreateProfileSignupRequest) o;
        return Objects.equals(getApplicationId(), that.getApplicationId()) && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getImageUrl(), that.getImageUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApplicationId(), getDisplayName(), getImageUrl());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProfileSignupRequest{");
        sb.append("applicationId='").append(applicationId).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", imageUrl='").append(imageUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
