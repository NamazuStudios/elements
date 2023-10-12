package dev.getelements.elements.model.profile;

import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;
import java.util.Objects;

@ApiModel(description = "Represents a request to update a profile.")
public class UpdateProfileRequest {

    /** @deprecated use imageObject */
    @Deprecated
    @ApiModelProperty("A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @ApiModelProperty("Image object stored in EL large objects storage.")
    private LargeObjectReference imageObject;

    @ApiModelProperty("A non-unique display name for this profile.")
    private String displayName;

    @ApiModelProperty("A map of arbitrary metadata.")
    private Map<String, Object> metadata;

    /**
     * @deprecated
     * Providing the entire {@link User} object is no longer necessary.
     */
    @Deprecated
    @ApiModelProperty(hidden = true)
    private User user;

    /**
     * @deprecated
     * Providing the entire {@link Application} object is no longer necessary.
     */
    @Deprecated
    @ApiModelProperty(hidden = true)
    private Application application;

    /**
     * @deprecated
     * Providing ID is no longer necessary.
     */
    @Deprecated
    @ApiModelProperty(hidden = true)
    private String id;

    /**
     * @deprecated
     * Providing lastLogin is no longer necessary.
     */
    @Deprecated
    @ApiModelProperty(hidden = true)
    private String lastLogin;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
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
        UpdateProfileRequest that = (UpdateProfileRequest) o;
        return Objects.equals(imageUrl, that.imageUrl) && Objects.equals(imageObject, that.imageObject) && Objects.equals(displayName, that.displayName) && Objects.equals(metadata, that.metadata) && Objects.equals(user, that.user) && Objects.equals(application, that.application) && Objects.equals(id, that.id) && Objects.equals(lastLogin, that.lastLogin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageUrl, imageObject, displayName, metadata, user, application, id, lastLogin);
    }

    @Override
    public String toString() {
        return "UpdateProfileRequest{" +
                "imageUrl='" + imageUrl + '\'' +
                ", imageObject=" + imageObject +
                ", displayName='" + displayName + '\'' +
                ", metadata=" + metadata +
                ", user=" + user +
                ", application=" + application +
                ", id='" + id + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                '}';
    }
}
