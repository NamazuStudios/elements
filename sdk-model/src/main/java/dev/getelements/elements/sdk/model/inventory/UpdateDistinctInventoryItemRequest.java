package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;


import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Represents a request to update a distinct inventory item's ownership and metadata. */
@Schema
public class UpdateDistinctInventoryItemRequest {

    /** Creates a new instance. */
    public UpdateDistinctInventoryItemRequest() {}

    @Schema(description = "The id of the User owning this inventory item id.")
    private String userId;

    @Schema(description = "The the profile id of hte Profile owning this inventory item.")
    private String profileId;

    private Map<String, Object> metadata;

    /**
     * Returns the ID of the user owning this inventory item.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user owning this inventory item.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the profile ID of the profile owning this inventory item.
     *
     * @return the profile ID
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Sets the profile ID of the profile owning this inventory item.
     *
     * @param profileId the profile ID
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * Returns the metadata for this inventory item update.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for this inventory item update.
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
        UpdateDistinctInventoryItemRequest that = (UpdateDistinctInventoryItemRequest) o;
        return Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getProfileId(), getMetadata());
    }

}
