package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Map;

/** Represents the request body for creating a new application. */
@Schema
public class CreateApplicationRequest implements Serializable {

    /** Creates a new instance. */
    public CreateApplicationRequest() {}

    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    private String name;

    private String description;

    private Map<String, Object> attributes;

    @Min(0)
    @Schema(description = "The maximum number of profiles a user may create for this application. " +
            "If unspecified, defaults to 1.")
    private Integer maxProfiles;

    @Schema(description = "Whether a user's primary profile for this application should be created " +
            "automatically when the user is created. If unspecified, defaults to true.")
    private Boolean autoCreateProfile;

    @Schema(description = "If true, a user cannot edit their own profile picture for this application via the " +
            "REST API -- it must be set by backend/Element code instead. If false (the default), users may edit " +
            "their own profile picture via the REST API.")
    private Boolean authoritativeProfilePicture;

    @Schema(description = "A Java regular expression that a profile's display name must match for this " +
            "application, or the profile create/update is rejected. If blank or unspecified, no additional " +
            "check is performed.")
    private String displayNameRegex;

    /**
     * Returns the application description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the application description.
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the unique application name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique application name.
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the application attributes.
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Sets the application attributes.
     * @param attributes the attributes
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets the maximum number of profiles a user may create for this application. If null (unspecified), the
     * server treats this as {@code 1}.
     *
     * @return the maximum number of profiles per user, or null if unspecified
     */
    public Integer getMaxProfiles() {
        return maxProfiles;
    }

    /**
     * Sets the maximum number of profiles a user may create for this application.
     *
     * @param maxProfiles the maximum number of profiles per user
     */
    public void setMaxProfiles(Integer maxProfiles) {
        this.maxProfiles = maxProfiles;
    }

    /**
     * Gets whether a user's primary profile for this application should be created automatically when the user
     * is created. If null (unspecified), the server treats this as {@code true}.
     *
     * @return whether to automatically create a primary profile, or null if unspecified
     */
    public Boolean getAutoCreateProfile() {
        return autoCreateProfile;
    }

    /**
     * Sets whether a user's primary profile for this application should be created automatically when the user
     * is created.
     *
     * @param autoCreateProfile whether to automatically create a primary profile
     */
    public void setAutoCreateProfile(Boolean autoCreateProfile) {
        this.autoCreateProfile = autoCreateProfile;
    }

    /**
     * Gets whether a user can edit their own profile picture for this application via the REST API. If null
     * (unspecified), the server treats this as {@code false}.
     *
     * @return whether the profile picture is authoritative (backend-only), or null if unspecified
     */
    public Boolean getAuthoritativeProfilePicture() {
        return authoritativeProfilePicture;
    }

    /**
     * Sets whether a user can edit their own profile picture for this application via the REST API.
     *
     * @param authoritativeProfilePicture whether the profile picture is authoritative (backend-only)
     */
    public void setAuthoritativeProfilePicture(Boolean authoritativeProfilePicture) {
        this.authoritativeProfilePicture = authoritativeProfilePicture;
    }

    /**
     * Gets the Java regular expression a profile's display name must match for this application. If null or
     * blank, no additional check is performed.
     *
     * @return the display name regular expression, or null if unspecified
     */
    public String getDisplayNameRegex() {
        return displayNameRegex;
    }

    /**
     * Sets the Java regular expression a profile's display name must match for this application.
     *
     * @param displayNameRegex the display name regular expression
     */
    public void setDisplayNameRegex(String displayNameRegex) {
        this.displayNameRegex = displayNameRegex;
    }
}
