package com.namazustudios.socialengine.model.profile;

import com.namazustudios.socialengine.model.application.EventDefinition;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.application.Application;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

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

    @NotNull(groups = Update.class)
    @Null(groups = {Insert.class, Create.class})
    @ApiModelProperty("The unique ID of the profile itself.")
    protected String id;

    @NotNull(groups = Insert.class)
    @ApiModelProperty("The User associated with this Profile.")
    private User user;

    @NotNull(groups = Insert.class)
    @ApiModelProperty("The Application associated with this Profile.")
    private Application application;

    @ApiModelProperty("A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @NotNull
    @ApiModelProperty("A non-unique display name for this profile.")
    private String displayName;

    @ApiModelProperty("An object containing arbitrary player metadata as key-value pairs.")
    private Map<String, Object> metadata;

    @ApiModelProperty("The last time this profile has been logged in by the user.")
    private long lastLogin;

    @ApiModelProperty("The event module to invoke on creation.")
    private EventDefinition eventDefinition;

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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public EventDefinition getEventDefinition() {
        return eventDefinition;
    }

    public void setEventDefinition(EventDefinition eventDefinition) {
        this.eventDefinition = eventDefinition;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Profile)) return false;
        Profile profile = (Profile) object;
        return  getLastLogin() == profile.getLastLogin() &&
                Objects.equals(getId(), profile.getId()) &&
                Objects.equals(getUser(), profile.getUser()) &&
                Objects.equals(getApplication(), profile.getApplication()) &&
                Objects.equals(getImageUrl(), profile.getImageUrl()) &&
                Objects.equals(getDisplayName(), profile.getDisplayName()) &&
                Objects.equals(metadata, profile.metadata);
    }

    @Override
    public int hashCode() {
        Long lastLogin = getLastLogin();    // cast to object for the hash
        return Objects.hash(getId(), getUser(), getApplication(), getImageUrl(), getDisplayName(), lastLogin, metadata);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", application=" + application +
                ", imageUrl='" + imageUrl + '\'' +
                ", displayName='" + displayName + '\'' +
                ", metadata=" + metadata +
                ", lastLogin=" + lastLogin +
                '}';
    }

}
