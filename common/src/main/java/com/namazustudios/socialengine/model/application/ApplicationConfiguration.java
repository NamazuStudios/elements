package com.namazustudios.socialengine.model.application;

import javax.validation.constraints.NotNull;

/**
 * Ties the {@link Application} model to one of its associated profiles as represented by the {@link Platform}
 * enumeration.  This is an abstract base class from which all application profiles are derived.
 *
 * Created by patricktwohig on 7/10/15.
 */
public class ApplicationConfiguration {

    private String id;

    @NotNull
    private Platform platform;

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

}
