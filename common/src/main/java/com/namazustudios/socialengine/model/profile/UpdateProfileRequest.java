package com.namazustudios.socialengine.model.profile;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Represents a request to update a profile.")
public class UpdateProfileRequest {

    @ApiModelProperty("A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @ApiModelProperty("A non-unique display name for this profile.")
    private String displayName;

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
