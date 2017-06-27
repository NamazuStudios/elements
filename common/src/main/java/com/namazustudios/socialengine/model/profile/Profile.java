package com.namazustudios.socialengine.model.profile;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Represents a user's profile.  Generally speaking a profile associates the {@link Application}
 * and the {@link User} together.  Multiple profiles linking the same user to the same application
 * should not exist together.
 *
 * Created by patricktwohig on 6/27/17.
 */
@ApiModel
public class Profile {

    @ApiModelProperty("The unique ID of the profile itself.")
    private String id;

    @NotNull
    @ApiModelProperty("The User associated with this Profile.")
    private User user;

    @NotNull
    @ApiModelProperty("The Application associated with this Profile.")
    private Application application;

    @ApiModelProperty("A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @NotNull
    @ApiModelProperty("A non-unique display name for this profile.")
    private String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
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
