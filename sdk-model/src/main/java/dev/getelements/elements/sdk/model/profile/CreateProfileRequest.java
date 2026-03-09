package dev.getelements.elements.sdk.model.profile;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

/** Represents a request to create a profile for a user. */
@Schema(description = "Represents a request to create a profile for a user.")
public class CreateProfileRequest {

    /** Creates a new instance. */
    public CreateProfileRequest() {}

    @NotNull
    @Schema(description = "The user id this profile belongs to.")
    private String userId;

    @NotNull
    @Schema(description = "The application id this profile belongs to.")
    private String applicationId;

    /** @deprecated use separate call to create image largeObject*/
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
     * Provide {@link #userId} instead
     */
    @Deprecated
    @Schema(hidden = true)
    private User user;

    /**
     * @deprecated
     * Providing the entire {@link Application} object is no longer necessary.
     * Provide {@link #applicationId} instead
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
     * @return the ID
     * @deprecated Providing ID is no longer necessary.
     */
    @Deprecated
    public String getId() {
        return id;
    }

    /**
     * Sets the profile ID.
     *
     * @param id the ID
     * @deprecated Providing ID is no longer necessary.
     */
    @Deprecated
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the user ID this profile belongs to.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the user this profile belongs to.
     *
     * @return the user
     * @deprecated use {@link #getUserId()} instead
     */
    @Deprecated
    public User getUser() {
        return user;
    }

    /**
     * Sets the user ID this profile belongs to.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the user this profile belongs to.
     *
     * @param user the user
     * @deprecated use {@link #setUserId(String)} instead
     */
    @Deprecated
    public void setUser(User user){
        this.user = user;
    }

    /**
     * Returns the application ID this profile belongs to.
     *
     * @return the application ID
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID this profile belongs to.
     *
     * @param applicationId the application ID
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the application this profile belongs to.
     *
     * @return the application
     * @deprecated use {@link #getApplicationId()} instead
     */
    @Deprecated
    public Application getApplication() {
        return application;
    }

    /**
     * Sets the application this profile belongs to.
     *
     * @param application the application
     * @deprecated use {@link #setApplicationId(String)} instead
     */
    @Deprecated
    public void setApplication(Application application){
        this.application = application;
    }

    /**
     * Returns the image URL for this profile.
     *
     * @return the image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image URL for this profile.
     *
     * @param imageUrl the image URL
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Returns the display name for this profile.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for this profile.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the metadata for this profile.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for this profile.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the last login timestamp.
     *
     * @return the last login
     * @deprecated Providing lastLogin is no longer necessary.
     */
    @Deprecated
    public String getLastLogin() {
        return lastLogin;
    }

    /**
     * Sets the last login timestamp.
     *
     * @param lastLogin the last login
     * @deprecated Providing lastLogin is no longer necessary.
     */
    @Deprecated
    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateProfileRequest that = (CreateProfileRequest) o;
        return Objects.equals(userId, that.userId) && Objects.equals(applicationId, that.applicationId) && Objects.equals(imageUrl, that.imageUrl) && Objects.equals(displayName, that.displayName) && Objects.equals(metadata, that.metadata) && Objects.equals(user, that.user) && Objects.equals(application, that.application) && Objects.equals(id, that.id) && Objects.equals(lastLogin, that.lastLogin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, applicationId, imageUrl, displayName, metadata, user, application, id, lastLogin);
    }

    @Override
    public String toString() {
        return "CreateProfileRequest{" +
                "userId='" + userId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", displayName='" + displayName + '\'' +
                ", metadata=" + metadata +
                ", user=" + user +
                ", application=" + application +
                ", id='" + id + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                '}';
    }
}
