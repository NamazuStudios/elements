package com.namazustudios.socialengine.model.profile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel(description = "Used in conjunction with the user creation request to specify.")
public class ProfileSignupRequest {

    @NotNull
    @ApiModelProperty("The application id this profile belongs to.")
    private String applicationId;

    @ApiModelProperty("A non-unique display name for this profile.")
    private String displayName;

    @ApiModelProperty("A URL to the image of the profile.  (ie the User's Avatar).")
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
        ProfileSignupRequest that = (ProfileSignupRequest) o;
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
