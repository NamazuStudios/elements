package com.namazustudios.socialengine.model.profile;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Represents a request to create a profile for a user.")
public class CreateProfileRequest {

    @ApiModelProperty("The user id this profile belongs to.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String userId;

    @ApiModelProperty("The application id this profile belongs to.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String applicationId;

    @ApiModelProperty("A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @NotNull
    @ApiModelProperty("A non-unique display name for this profile.")
    private String displayName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
