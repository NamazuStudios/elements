package com.namazustudios.socialengine.model.inventory;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;
import java.util.Objects;

@ApiModel
public class DistinctInventoryItem {

    @Null(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The unique ID of the inventory item itself.")
    private String id;

    private Item item;

    private User user;

    private Profile profile;

    private Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
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
        DistinctInventoryItem that = (DistinctInventoryItem) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getItem(), that.getItem()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getProfile(), that.getProfile()) && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getItem(), getUser(), getProfile(), getMetadata());
    }

}
