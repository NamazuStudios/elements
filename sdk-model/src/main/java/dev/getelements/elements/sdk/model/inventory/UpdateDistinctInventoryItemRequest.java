package dev.getelements.elements.sdk.model.inventory;

import io.swagger.v3.oas.annotations.media.Schema;


import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Schema
public class UpdateDistinctInventoryItemRequest {

    @Schema(description = "The id of the User owning this inventory item id.")
    private String userId;

    @Schema(description = "The the profile id of hte Profile owning this inventory item.")
    private String profileId;

    private Map<String, Object> metadata;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

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
