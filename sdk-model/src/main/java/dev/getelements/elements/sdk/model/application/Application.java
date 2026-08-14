package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.ValidationGroups.*;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an application.  This serves as place to house the
 * application's basic metadata such as app id and display name.
 *
 * Since an application can exist on multiple platforms, this seeks
 * to simply tie all platforms together.
 *
 * Created by patricktwohig on 7/9/15.
 */
@Schema
public class Application implements Serializable {

    /**
     * Used as the key for the application attribute where appropriate.  This is equivalent
     * to the FQN of the {@link Application} class.
     */
    public static final String APPLICATION_ATTRIBUTE = Application.class.getName();

    /** Creates a new instance. */
    public Application() {}

    @Null(groups = Insert.class)
    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    private String name;

    private String description;

    private String gitBranch;

    private String scriptRepoUrl;

    private String httpDocumentationUrl;

    private String httpDocumentationUiUrl;

    private String httpTunnelEndpointUrl;

    private Map<String, Object> attributes;

    @Null(groups = {Create.class})
    private ApplicationConfiguration applicationConfiguration;

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
     * The globally-unique identifier.
     *
     * @return the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * The identifier.
     *
     * @param id the identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the unique name of the application.  Must be alpha-numeric
     * as it is used to form REST URLs.
     *
     * @return the unique name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the application.
     * @param name the unique name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the git branch of the application. If blank, assume the default branch.
     *
     * @return the git branch
     */
    public String getGitBranch() {
        return gitBranch;
    }

    /**
     * Sets the git branch of the application. If blank, assume the default branch.
     * @param gitBranch the git branch
     */
    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    /**
     * Gets the description.  This is just an optional description for
     * the application used for reference.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets script repo URL.
     *
     * @return the script repo URL
     */
    public String getScriptRepoUrl() {
        return scriptRepoUrl;
    }

    /**
     * Sets the script repo URL.
     *
     * @param scriptRepoUrl the script repo URL
     */
    public void setScriptRepoUrl(String scriptRepoUrl) {
        this.scriptRepoUrl = scriptRepoUrl;
    }

    /**
     * Gets the HTTP documentation endpoint for the application if it is available.  Otherwise null.
     *
     * @return the HTTP documentation endpoint
     */
    public String getHttpDocumentationUrl() {
        return httpDocumentationUrl;
    }

    /**
     * Sets the HTTP documentation endpoint for the application if it is available.  Otherwise null.
     *
     * @param httpDocumentationUrl  the HTTP documentation endpoint
     */
    public void setHttpDocumentationUrl(String httpDocumentationUrl) {
        this.httpDocumentationUrl = httpDocumentationUrl;
    }

    /**
     * Gets the HTTP documentation UI URL for the application if it is available.  Otherwise null.
     *
     * @return the HTTP documentation UI url
     */
    public String getHttpDocumentationUiUrl() {
        return httpDocumentationUiUrl;
    }

    /**
     * Sets the HTTP documentation UI URL for the application if it is available.  Otherwise null.
     *
     * @param httpDocumentationUiUrl  the HTTP documentation UI url
     */
    public void setHttpDocumentationUiUrl(String httpDocumentationUiUrl) {
        this.httpDocumentationUiUrl = httpDocumentationUiUrl;
    }

    /**
     * Gets the HTTP tunnel endpoint, if this is available.  Otherwise null.
     *
     * @return the HTTP tunnel endpoint
     */
    public String getHttpTunnelEndpointUrl() {
        return httpTunnelEndpointUrl;
    }

    /**
     * Sets the HTTP tunnel endpoint, if this is available.  Otherwise null.
     *
     * @param httpTunnelEndpointUrl  the HTTP tunnel endpoint
     */
    public void setHttpTunnelEndpointUrl(String httpTunnelEndpointUrl) {
        this.httpTunnelEndpointUrl = httpTunnelEndpointUrl;
    }

    /**
     * Gets the application configuration for this application, if this is available.  Otherwise null.
     *
     * @return the application configuration
     */
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    /**
     * Sets the application configuration.
     *
     * @param applicationConfiguration  the application configuration
     */
    public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getGitBranch(), that.getGitBranch()) && Objects.equals(getScriptRepoUrl(), that.getScriptRepoUrl()) && Objects.equals(getHttpDocumentationUrl(), that.getHttpDocumentationUrl()) && Objects.equals(getHttpDocumentationUiUrl(), that.getHttpDocumentationUiUrl()) && Objects.equals(getHttpTunnelEndpointUrl(), that.getHttpTunnelEndpointUrl()) && Objects.equals(getAttributes(), that.getAttributes()) && Objects.equals(getApplicationConfiguration(), that.getApplicationConfiguration()) && Objects.equals(getMaxProfiles(), that.getMaxProfiles()) && Objects.equals(getAutoCreateProfile(), that.getAutoCreateProfile()) && Objects.equals(getAuthoritativeProfilePicture(), that.getAuthoritativeProfilePicture()) && Objects.equals(getDisplayNameRegex(), that.getDisplayNameRegex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getGitBranch(), getScriptRepoUrl(), getHttpDocumentationUrl(), getHttpDocumentationUiUrl(), getHttpTunnelEndpointUrl(), getAttributes(), getApplicationConfiguration(), getMaxProfiles(), getAutoCreateProfile(), getAuthoritativeProfilePicture(), getDisplayNameRegex());
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

}
