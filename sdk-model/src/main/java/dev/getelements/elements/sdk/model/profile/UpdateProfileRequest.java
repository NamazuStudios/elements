package dev.getelements.elements.sdk.model.profile;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.Map;
import java.util.Objects;

/** Represents a request to update a profile's properties. */
@Schema(description = "Represents a request to update a profile.")
public class UpdateProfileRequest {

    /** Creates a new instance. */
    public UpdateProfileRequest() {}

    /**
     * A URL to the image of the profile.
     *
     * @deprecated use separate call to update image largeObject
     */
    @Deprecated
    @Schema(description = "A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @Schema(description = "A non-unique display name for this profile.")
    private String displayName;

    @Schema(description = "A map of arbitrary metadata.")
    private Map<String, Object> metadata;

    /**
     * @deprecated
     * Providing the entire {@link User} object is no longer necessary.
     */
    @Deprecated
    @Schema(hidden = true)
    private User user;

    /**
     * @deprecated
     * Providing the entire {@link Application} object is no longer necessary.
     */
    @Deprecated
    @Schema(hidden = true)
    private Application application;

    /**
     * @deprecated
     * Providing ID is no longer necessary.
     */
    @Deprecated
    @Schema(hidden = true)
    private String id;

    /**
     * @deprecated
     * Providing lastLogin is no longer necessary.
     */
    @Deprecated
    @Schema(hidden = true)
    private String lastLogin;

    /**
     * Returns the profile ID.
     *
     * @return the id
     * @deprecated providing ID is no longer necessary
     */
    @Deprecated
    public String getId() {
        return id;
    }

    /**
     * Sets the profile ID.
     *
     * @param id the id
     * @deprecated providing ID is no longer necessary
     */
    @Deprecated
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the last login time.
     *
     * @return the last login
     * @deprecated providing lastLogin is no longer necessary
     */
    @Deprecated
    public String getLastLogin() {
        return lastLogin;
    }

    /**
     * Sets the last login time.
     *
     * @param lastLogin the last login
     * @deprecated providing lastLogin is no longer necessary
     */
    @Deprecated
    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Returns the URL to the profile image.
     *
     * @return the image URL
     * @deprecated use separate call to update image largeObject
     */
    @Deprecated
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the URL to the profile image.
     *
     * @param imageUrl the image URL
     * @deprecated use separate call to update image largeObject
     */
    @Deprecated
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Returns the display name for the profile.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for the profile.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the metadata for the profile.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for the profile.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the user associated with this profile.
     *
     * @return the user
     * @deprecated providing the entire User object is no longer necessary
     */
    @Deprecated
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this profile.
     *
     * @param user the user
     * @deprecated providing the entire User object is no longer necessary
     */
    @Deprecated
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the application associated with this profile.
     *
     * @return the application
     * @deprecated providing the entire Application object is no longer necessary
     */
    @Deprecated
    public Application getApplication() {
        return application;
    }

    /**
     * Sets the application associated with this profile.
     *
     * @param application the application
     * @deprecated providing the entire Application object is no longer necessary
     */
    @Deprecated
    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateProfileRequest that = (UpdateProfileRequest) o;
        return Objects.equals(imageUrl, that.imageUrl) && Objects.equals(displayName, that.displayName) && Objects.equals(metadata, that.metadata) && Objects.equals(user, that.user) && Objects.equals(application, that.application) && Objects.equals(id, that.id) && Objects.equals(lastLogin, that.lastLogin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageUrl, displayName, metadata, user, application, id, lastLogin);
    }

    @Override
    public String toString() {
        return "UpdateProfileRequest{" +
                "imageUrl='" + imageUrl + '\'' +
                ", displayName='" + displayName + '\'' +
                ", metadata=" + metadata +
                ", user=" + user +
                ", application=" + application +
                ", id='" + id + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                '}';
    }
}
