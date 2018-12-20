package com.namazustudios.socialengine.model.mission;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Represents a Reward that has been issued but has not yet been claimed by the user.  The " +
                        "reward is assigned a unique ID to ensure that it may not have been applied more than once.")
public class PendingReward implements Serializable {

    @Null(groups = {Create.class, Insert.class})
    @NotNull(groups = {Update.class})
    @ApiModelProperty("The databased-assigned unique ID of the pending reward.")
    private String id;

    @NotNull
    @ApiModelProperty("The User to receive the reward.")
    private User user;

    @NotNull
    @ApiModelProperty("The state of the reward.")
    private State state;

    @ApiModelProperty("The reward to issue when this pending reward is claimed.")
    private Reward reward;

    @ApiModelProperty("The step that was completed to earn the reward.")
    private Step step;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PendingReward)) return false;
        PendingReward that = (PendingReward) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                getState() == that.getState() &&
                Objects.equals(getReward(), that.getReward()) &&
                Objects.equals(getStep(), that.getStep());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getState(), getReward(), getStep());
    }

    @Override
    public String toString() {
        return "PendingReward{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", state=" + state +
                ", reward=" + reward +
                ", step=" + step +
                '}';
    }

    public enum State {

        /**
         * Indicates that the reward has been created fresh.
         */
        CREATED,

        /**
         * Indicates that the reward is in a state of pending.
         */
        PENDING,

        /**
         * Indicates that the reward is in the rewarded state. Once rewarded, the system may delete the reward
         * after some time.
         */
        REWARDED

    }

}
