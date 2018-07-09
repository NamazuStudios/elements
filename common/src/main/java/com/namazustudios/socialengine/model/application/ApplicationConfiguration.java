package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;

/**
 * Ties the {@link Application} model to one of its associated profiles as represented by the {@link ConfigurationCategory}
 * enumeration.  This is an abstract base class from which all application profiles are derived.
 *
 * Created by patricktwohig on 7/10/15.
 */
@ApiModel
public class ApplicationConfiguration implements Serializable {

    @ApiModelProperty("The database assigned ID for the application configuration.")
    private String id;

    @NotNull
    @ApiModelProperty("The category for the application configuration.")
    private ConfigurationCategory category;

    @ApiModelProperty("The application-configuration specific uinique ID.  (Varies by ConfigurationCategory)")
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
    public ConfigurationCategory getCategory() {
        return category;
    }

    /**
     * Sets the category identifier.
     *
     * @param category the category identifier type.
     */
    public void setCategory(ConfigurationCategory category) {
        this.category = category;
    }

    /**
     * Gets the unique identifier for the category.
     *
     * @return
     */
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * Sets the unique identifier for the category.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationConfiguration)) return false;

        ApplicationConfiguration that = (ApplicationConfiguration) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getCategory() != that.getCategory()) return false;
        if (getUniqueIdentifier() != null ? !getUniqueIdentifier().equals(that.getUniqueIdentifier()) : that.getUniqueIdentifier() != null)
            return false;
        return getParent() != null ? getParent().equals(that.getParent()) : that.getParent() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getCategory() != null ? getCategory().hashCode() : 0);
        result = 31 * result + (getUniqueIdentifier() != null ? getUniqueIdentifier().hashCode() : 0);
        result = 31 * result + (getParent() != null ? getParent().hashCode() : 0);
        return result;
    }

}
