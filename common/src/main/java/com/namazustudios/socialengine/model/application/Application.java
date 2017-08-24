package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Represents an application.  This serves as place to house the
 * application's basic metadata such as app id and display name.
 *
 * Since an application can exist on multiple platforms, this seeks
 * to simply tie all platforms together.
 *
 * Created by patricktwohig on 7/9/15.
 */
@ApiModel
public class Application {

    /**
     * Used as the key for the application attribute where appropriate.  This is equivalent
     * to the FQN of the {@link Application} class.
     */
    public static final String APPLICATION_ATTRIUTE = Application.class.getName();

    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WORD_ONLY)
    private String name;

    private String description;

    private String scriptRepoUrl;

    private String httpDocumentationUrl;

    private String httpTunnelEndpointUrl;

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
     * Gets the HTTP documentation endpoint for the application if it is availble.  Otherwise null.
     *
     * @return the HTTP documentation endpoint
     */
    public String getHttpDocumentationUrl() {
        return httpDocumentationUrl;
    }

    /**
     * Gets the HTTP documentation endpoint for the application if it is availble.  Otherwise null.
     *
     * @param httpDocumentationUrl  the HTTP documentation endpoint
     */
    public void setHttpDocumentationUrl(String httpDocumentationUrl) {
        this.httpDocumentationUrl = httpDocumentationUrl;
    }

    /**
     * Gets the HTTP tunnel endpoint, if this is availble.  Otherwise null.
     *
     * @return the HTTP tunnel endpoint
     */
    public String getHttpTunnelEndpointUrl() {
        return httpTunnelEndpointUrl;
    }

    /**
     * Sets the HTTP tunnel endpoint, if this is availble.  Otherwise null.
     *
     * @param httpTunnelEndpointUrl  the HTTP tunnel endpoint
     */
    public void setHttpTunnelEndpointUrl(String httpTunnelEndpointUrl) {
        this.httpTunnelEndpointUrl = httpTunnelEndpointUrl;
    }

}
