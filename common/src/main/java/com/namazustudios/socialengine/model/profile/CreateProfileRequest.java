package com.namazustudios.socialengine.model.profile;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Represents a request to create a profile for a user.")
public class CreateProfileRequest {

    @ApiModelProperty("The user id this profile belongs to.")
    @NotNull
    private String userId;

    @ApiModelProperty("The application id this profile belongs to.")
    @NotNull
    private String applicationId;

    /**
     * @deprecated
     * Providing the entire {@link User} object is no longer necessary.
     * Provide {@link #userId} instead
     */
    @Deprecated
    @ApiModelProperty(hidden = true)
    private User user;

    /**
     * @deprecated
     * Providing the entire {@link Application} object is no longer necessary.
     * Provide {@link #applicationId} instead
     */
    @Deprecated
    @ApiModelProperty(hidden = true)
    private Application application;

    @ApiModelProperty("A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @NotNull
    @ApiModelProperty("A non-unique display name for this profile.")
    private String displayName;

    public String getUserId() {
        return userId != null ? userId : user.getId();
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUser(User user){
        this.user = user;
    }

    public String getApplicationId() {
        return applicationId != null ? applicationId : application.getId();
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setApplication(Application application){
        this.application = application;
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
