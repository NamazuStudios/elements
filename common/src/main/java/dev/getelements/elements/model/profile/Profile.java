package dev.getelements.elements.model.profile;

import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Update;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
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
     * Alpha-numeric only.  Allows underscore, but does not allow the word to start with an underscore.
     */
    public static final String PROFILE_DISPLAY_NAME_REGEX = "[^ '\\-_.][A-Za-z0-9 '\\-_.]+$";

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

    /** @deprecated use reference to LargeObject with url */
    @Deprecated
    @ApiModelProperty("A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @ApiModelProperty("Image object stored in EL large objects storage.")
    private LargeObjectReference imageObject;

    @NotNull
    @Pattern(regexp = PROFILE_DISPLAY_NAME_REGEX)
    @ApiModelProperty("A non-unique display name for this profile.")
    private String displayName;

    @ApiModelProperty("An object containing arbitrary player metadata as key-value pairs.")
    private Map<String, Object> metadata;

    @ApiModelProperty("The last time this profile has been logged in by the user.")
    private long lastLogin;

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

    public LargeObjectReference getImageObject() {
        return imageObject;
    }

    public void setImageObject(LargeObjectReference imageObject) {
        this.imageObject = imageObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return lastLogin == profile.lastLogin && Objects.equals(id, profile.id) && Objects.equals(user, profile.user) && Objects.equals(application, profile.application) && Objects.equals(imageUrl, profile.imageUrl) && Objects.equals(imageObject, profile.imageObject) && Objects.equals(displayName, profile.displayName) && Objects.equals(metadata, profile.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, application, imageUrl, imageObject, displayName, metadata, lastLogin);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", application=" + application +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageObject=" + imageObject +
                ", displayName='" + displayName + '\'' +
                ", metadata=" + metadata +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
