package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.ValidationGroups.*;
import io.swagger.v3.oas.annotations.media.Schema;

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
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the application.
     * @param name
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
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getGitBranch(), that.getGitBranch()) && Objects.equals(getScriptRepoUrl(), that.getScriptRepoUrl()) && Objects.equals(getHttpDocumentationUrl(), that.getHttpDocumentationUrl()) && Objects.equals(getHttpDocumentationUiUrl(), that.getHttpDocumentationUiUrl()) && Objects.equals(getHttpTunnelEndpointUrl(), that.getHttpTunnelEndpointUrl()) && Objects.equals(getAttributes(), that.getAttributes()) && Objects.equals(getApplicationConfiguration(), that.getApplicationConfiguration());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getGitBranch(), getScriptRepoUrl(), getHttpDocumentationUrl(), getHttpDocumentationUiUrl(), getHttpTunnelEndpointUrl(), getAttributes(), getApplicationConfiguration());
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

}
