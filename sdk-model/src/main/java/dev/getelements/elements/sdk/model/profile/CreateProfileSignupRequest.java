package dev.getelements.elements.sdk.model.profile;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Used in conjunction with the user creation request to specify a profile. */
@Schema(description = "Used in conjunction with the user creation request to specify.")
public class CreateProfileSignupRequest {

    /** Creates a new instance. */
    public CreateProfileSignupRequest() {}

    @NotNull
    @Schema(description = "The application id this profile belongs to.")
    private String applicationId;

    @Schema(description = "A non-unique display name for this profile. If left null, the server will assign.")
    private String displayName;

    @Schema(description = "A URL to the image of the profile.  (ie the User's Avatar). If left null, the server will no assign any URL.")
    private String imageUrl;

    /**
     * Returns the application ID this profile belongs to.
     *
     * @return the application ID
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID this profile belongs to.
     *
     * @param applicationId the application ID
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the display name for this profile.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for this profile.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the image URL for this profile.
     *
     * @return the image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image URL for this profile.
     *
     * @param imageUrl the image URL
     */
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
