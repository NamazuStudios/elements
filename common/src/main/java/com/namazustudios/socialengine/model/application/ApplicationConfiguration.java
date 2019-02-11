package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
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

    @ApiModelProperty("The application-configuration specific unique ID.  (Varies by ConfigurationCategory)")
    private String uniqueIdentifier;

    @ApiModelProperty("The parent application owning this configuration.")
    @NotNull
    private Application parent;

    @ApiModelProperty("A mapping of IAP product ids to Item ids in the db. E.g. if we have a productId " +
            "'pack_10_coins' representing 'Pack of 10 Coins', and we have itemId 'a1b2c3' for the Coin Item in the " +
            "db, then we should have a kv-pair of 'pack_10_coins': 'a1b2c3'.")
    // iap product ids to item ids
    private Map<String, String> iapProductIdsToItemIds;

    @ApiModelProperty("A mapping of IAP product ids in the db to the quantity set when creating a Reward. E.g. for " +
            "a productId 'pack_10_coins' representing 'Pack of 10 Coins', we should have a kv-pair of " +
            "'pack_10_coins': 10 .")
    private Map<String, Integer> iapProductIdsToRewardQuantities;

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

    public Map<String, String> getIapProductIdsToItemIds() {
        return iapProductIdsToItemIds;
    }

    public void setIapProductIdsToItemIds(Map<String, String> iapProductIdsToItemIds) {
        this.iapProductIdsToItemIds = iapProductIdsToItemIds;
    }

    public Map<String, Integer> getIapProductIdsToRewardQuantities() {
        return iapProductIdsToRewardQuantities;
    }

    public void setIapProductIdsToRewardQuantities(Map<String, Integer> iapProductIdsToRewardQuantities) {
        this.iapProductIdsToRewardQuantities = iapProductIdsToRewardQuantities;
    }

    public void addIapProductIdToItemId(final String productId, final String itemId) {

        if (getIapProductIdsToItemIds() == null) {
            setIapProductIdsToItemIds(new HashMap<>());
        }

        getIapProductIdsToItemIds().put(productId, itemId);
    }

    public void addIapProductIdToRewardQuantity(final String productId, final Integer rewardQuantity) {

        if (getIapProductIdsToRewardQuantities() == null) {
            setIapProductIdsToRewardQuantities(new HashMap<>());
        }

        getIapProductIdsToRewardQuantities().put(productId, rewardQuantity);
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
                Objects.equals(getIapProductIdsToItemIds(), that.getIapProductIdsToItemIds()) &&
                Objects.equals(getIapProductIdsToRewardQuantities(), that.getIapProductIdsToRewardQuantities());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCategory(), getUniqueIdentifier(), getParent(), getIapProductIdsToItemIds(),
                getIapProductIdsToRewardQuantities());
    }

    @Override
    public String toString() {
        return "ApplicationConfiguration{" +
                "id='" + id + '\'' +
                ", category=" + category +
                ", uniqueIdentifier='" + uniqueIdentifier + '\'' +
                ", parent=" + parent +
                ", iapProductIdsToItemIds=" + iapProductIdsToItemIds +
                ", iapProductIdsToRewardQuantities=" + iapProductIdsToRewardQuantities +
                '}';
    }

    public String getItemIdForProductId(String productId) {
        final Map<String, String> iapProductIdsToItemIds = getIapProductIdsToItemIds();

        if (iapProductIdsToItemIds == null) {
            throw new InvalidDataException("Application Configuration " + getId() +
                    "has no product id -> item id mapping.");
        }
        if (!iapProductIdsToItemIds.containsKey(productId)) {
            throw new NotFoundException("IAP product id " + productId + " is not in the application " +
                    "configuration " + getId() + "  product id -> item id " +
                    "mapping.");
        }

        return iapProductIdsToItemIds.get(productId);
    }

    public Integer getQuantityForProductId(String productId) {
        final Map<String, Integer> iapProductIdsToRewardQuantities = getIapProductIdsToRewardQuantities();

        if (iapProductIdsToRewardQuantities == null) {
            throw new InvalidDataException("Application Configuration " + getId() +
                    "has no product id -> reward quantity mapping.");
        }
        if (!iapProductIdsToRewardQuantities.containsKey(productId)) {
            throw new NotFoundException("IAP product id " + productId + " is not in the application " +
                    "configuration " + getId() + "  product id -> reward " +
                    "quantity mapping.");
        }

        return iapProductIdsToRewardQuantities.get(productId);
    }
}
