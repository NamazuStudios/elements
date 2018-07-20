package com.namazustudios.socialengine.model.profile;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Represents a user's profile.  Generally speaking a profile associates the {@link Application}
 * and the {@link User} together.  Multiple profiles linking the same user to the same application
 * should not exist together.
 *
 * Created by patricktwohig on 6/27/17.
 */
@ApiModel
public class Profile implements Serializable {

    /**
     * Used as the key for the profile attribute where appropriate.  This is equivalent
     * to the FQN of the {@link Profile} class.
     */
    public static final String PROFILE_ATTRIBUTE = Profile.class.getName();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;

        Profile profile = (Profile) o;

        if (getId() != null ? !getId().equals(profile.getId()) : profile.getId() != null) return false;
        if (getUser() != null ? !getUser().equals(profile.getUser()) : profile.getUser() != null) return false;
        if (getApplication() != null ? !getApplication().equals(profile.getApplication()) : profile.getApplication() != null)
            return false;
        if (getImageUrl() != null ? !getImageUrl().equals(profile.getImageUrl()) : profile.getImageUrl() != null)
            return false;
        return getDisplayName() != null ? getDisplayName().equals(profile.getDisplayName()) : profile.getDisplayName() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getApplication() != null ? getApplication().hashCode() : 0);
        result = 31 * result + (getImageUrl() != null ? getImageUrl().hashCode() : 0);
        result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", application=" + application +
                ", imageUrl='" + imageUrl + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }

}
