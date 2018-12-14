package com.namazustudios.socialengine.model.mission;

import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;

@ApiModel(description = "Represents a Reward that has been issued but has not yet been claimed by the user.  The " +
                        "reward is assigned a unique ID to ensure that it may not have been applied more than once.")
public class PendingReward {

    @Null(groups = {Create.class, Insert.class})
    @NotNull(groups = {Update.class})
    @ApiModelProperty("The databased-assigned unique ID of the pending reward.")
    private String id;

    @NotNull(groups = {Create.class, Update.class, Insert.class})
    @ApiModelProperty("The reward to issue when this pending reward is claimed.")
    private Reward reward;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PendingReward)) return false;
        PendingReward that = (PendingReward) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getReward(), that.getReward());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getReward());
    }

    @Override
    public String toString() {
        return "PendingReward{" +
                "id='" + id + '\'' +
                ", reward=" + reward +
                '}';
    }

}
