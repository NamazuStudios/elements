package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

/** Request model for creating a distinct inventory item. */
@Schema
public class CreateDistinctInventoryItemRequest {

    /** Creates a new instance. */
    public CreateDistinctInventoryItemRequest() {}

    @NotNull
    @Schema(description = "The digital goods item id.")
    private String itemId;

    @Schema(description = "The id of the User owning this inventory item id.")
    private String userId;

    @Schema(description = "The the profileid of hte Profile owning this inventory item.")
    private String profileId;

    private Map<String, Object> metadata;

    /**
     * Returns the item ID.
     *
     * @return the item ID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Sets the item ID.
     *
     * @param itemId the item ID
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Returns the user ID owning this inventory item.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID owning this inventory item.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the profile ID owning this inventory item.
     *
     * @return the profile ID
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Sets the profile ID owning this inventory item.
     *
     * @param profileId the profile ID
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * Returns the metadata for this item.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for this item.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateDistinctInventoryItemRequest that = (CreateDistinctInventoryItemRequest) o;
        return Objects.equals(getItemId(), that.getItemId()) && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItemId(), getUserId(), getProfileId(), getMetadata());
    }

}
