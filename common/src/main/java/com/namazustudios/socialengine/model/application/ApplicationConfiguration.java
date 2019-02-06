package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @ApiModelProperty("A mapping of IAP product identifiers to Item ids in the db")
    // iap product ids to item ids
    private Map<String, String> iapProductIdToItemIds;

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

    public Map<String, String> getIapProductIdToItemIds() {
        return iapProductIdToItemIds;
    }

    public void setIapProductIdToItemIds(Map<String, String> iapProductIdToItemIds) {
        this.iapProductIdToItemIds =
                iapProductIdToItemIds != null ? iapProductIdToItemIds : Collections.emptyMap();
    }

    public void addIapProductIdToItemIds(final String productId, final String itemId) {

        if (getIapProductIdToItemIds() == null) {
            setIapProductIdToItemIds(new HashMap<>());
        }

        getIapProductIdToItemIds().put(productId, itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationConfiguration that = (ApplicationConfiguration) o;
        return Objects.equals(getId(), that.getId()) &&
                getCategory() == that.getCategory() &&
                Objects.equals(getUniqueIdentifier(), that.getUniqueIdentifier()) &&
                Objects.equals(getParent(), that.getParent()) &&
                Objects.equals(getIapProductIdToItemIds(), that.getIapProductIdToItemIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCategory(), getUniqueIdentifier(), getParent(), getIapProductIdToItemIds());
    }
}
