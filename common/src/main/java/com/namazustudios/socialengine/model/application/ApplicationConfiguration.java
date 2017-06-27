package com.namazustudios.socialengine.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Ties the {@link Application} model to one of its associated profiles as represented by the {@link Platform}
 * enumeration.  This is an abstract base class from which all application profiles are derived.
 *
 * Created by patricktwohig on 7/10/15.
 */
@ApiModel
public class ApplicationConfiguration {

    @ApiModelProperty("The databased assigned ID for the application configuration.")
    private String id;

    @NotNull
    @ApiModelProperty("The platform for the application configuration.")
    private Platform platform;

    @ApiModelProperty("The application-configuration specific uinique ID.  (Varies by Platform)")
    private String uniqueIdentifier;

    @ApiModelProperty("The parent application owning this configuration.")
    @NotNull
    private Application parent;

    /**
     * Gets the actual profile ID.
     *
     * @return the profile ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the actual profile ID.
     *
     * @param id the profile ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the platfrom identifier.
     *
     * @return the identifier type
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Sets the platform identifier.
     *
     * @param platform the platform identifier type.
     */
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    /**
     * Gets the unique identifier for the platform.
     *
     * @return
     */
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * Sets the unique identifier for the platform.
     *
     * @param uniqueIdentifier
     */
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    /**
     * Gets the parent {@link Application}
     *
     * @return the parent
     */
    public Application getParent() {
        return parent;
    }

    /**
     * Sets the parent {@link Application}
     *
     * @param parent the parent
     */
    public void setParent(Application parent) {
        this.parent = parent;
    }

}
