package com.namazustudios.socialengine.model.inventory;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

@ApiModel
public class CreateDistinctInventoryItemRequest {

    @NotNull
    @ApiModelProperty("The digital goods item id.")
    private String itemId;

    @ApiModelProperty("The id of the User owning this inventory item id.")
    private String userId;

    @ApiModelProperty("The the profileid of hte Profile owning this inventory item.")
    private String profileId;

    private Map<String, Object> metadata;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

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
        CreateDistinctInventoryItemRequest that = (CreateDistinctInventoryItemRequest) o;
        return Objects.equals(getItemId(), that.getItemId()) && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getProfileId(), that.getProfileId()) && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItemId(), getUserId(), getProfileId(), getMetadata());
    }

}
