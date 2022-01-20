package com.namazustudios.socialengine.model.inventory;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DistinctInventoryItem that = (DistinctInventoryItem) o;
        return Objects.equals(id, that.id) && Objects.equals(item, that.item) && Objects.equals(user, that.user) && Objects.equals(profile, that.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, item, user, profile);
    }

}
