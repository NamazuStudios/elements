package com.namazustudios.socialengine.model.mission;

import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Represents the mission state (i.e., progress).
 *
 * Created by davidjbrooks on 12/03/2018.
 */
public class Progress implements Serializable {

    @NotNull(groups={Update.class})
    @Null(groups={Create.class, Insert.class})
    @ApiModelProperty("The unique ID of the progress instance")
    private String id;

    @NotNull(groups={Create.class, Insert.class, Update.class})
    @ApiModelProperty("The profile of the owner of this progress")
    private Profile profile;

    @Null
    @ApiModelProperty("The current step")
    private Step currentStep;

    @Null
    @ApiModelProperty("The remaining actions")
    private Integer remaining;

    @NotNull(groups={Create.class, Insert.class, Update.class})
    @ApiModelProperty("The mission")
    private ProgressMissionInfo mission;

    @ApiModelProperty("List of all reward issuances that are issued but not expired, or redeemed but persistent.")
    private List<RewardIssuance> rewardIssuances;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) { this.profile = profile; }

    public Step getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    public Integer getRemaining() {
        return remaining;
    }

    public void setRemaining(Integer remaining) { this.remaining = remaining; }

    public ProgressMissionInfo getMission() {
        return mission;
    }

    public void setMission(ProgressMissionInfo mission) {
        this.mission = mission;
    }

    public List<RewardIssuance> getRewardIssuances() {
        return rewardIssuances;
    }

    public void setRewardIssuances(List<RewardIssuance> rewardIssuances) {
        this.rewardIssuances = rewardIssuances;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Progress)) return false;
        Progress progress = (Progress) object;
        return Objects.equals(getId(), progress.getId()) &&
                Objects.equals(getProfile(), progress.getProfile()) &&
                Objects.equals(getCurrentStep(), progress.getCurrentStep()) &&
                Objects.equals(getRemaining(), progress.getRemaining()) &&
                Objects.equals(getMission(), progress.getMission()) &&
                Objects.equals(getRewardIssuances(), progress.getRewardIssuances());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProfile(), getCurrentStep(), getRemaining(), getMission(), getRewardIssuances());
    }

    @Override
    public String toString() {
        return "Progress{" +
                "id='" + id + '\'' +
                ", profile=" + profile +
                ", currentStep=" + currentStep +
                ", remaining=" + remaining +
                ", mission=" + mission +
                ", rewardIssuances=" + rewardIssuances +
                '}';
    }

}
