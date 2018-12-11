package com.namazustudios.socialengine.model.mission;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.profile.Profile;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * Represents the mission state (i.e., progress).
 *
 * Created by davidjbrooks on 12/03/2018.
 */

public class Progress {

    @ApiModelProperty("The unique ID of the progress instance")
    @Null(groups={ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @NotNull(groups={ValidationGroups.Update.class})
    private String id;

    @ApiModelProperty("The profile of the owner of this progress")
    @Null(groups={ValidationGroups.Update.class})
    @NotNull(groups={ValidationGroups.Create.class, ValidationGroups.Insert.class})
    private Profile profile;

    @ApiModelProperty("The current step")
    @NotNull()
    private Step currentStep;

    @ApiModelProperty("The remaining actions")
    @NotNull()
    private Integer remaining;

    @ApiModelProperty("The mission")
    @Null(groups={ValidationGroups.Update.class})
    @NotNull(groups={ValidationGroups.Create.class, ValidationGroups.Insert.class})
    private Mission mission;


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

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Progress)) return false;

        Progress progress = (Progress) o;

        if (getId() != null ? !getId().equals(progress.getId()) : progress.getId() != null) return false;
        if (getProfile() != null ? !getProfile().equals(progress.getProfile()) : progress.getProfile() != null) return false;
        if (getCurrentStep() != null ? !getCurrentStep().equals(progress.getCurrentStep()) : progress.getCurrentStep() != null) return false;
        if (getRemaining() != null ? !getRemaining().equals(progress.getRemaining()) : progress.getRemaining() != null) return false;
        return (getMission() != null ? !getMission().equals(progress.getMission()) : progress.getMission() != null);
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        result = 31 * result + (getCurrentStep() != null ? getCurrentStep().hashCode() : 0);
        result = 31 * result + (getRemaining() != null ? getRemaining().hashCode() : 0);
        result = 31 * result + (getMission() != null ? getMission().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Progress{" +
                ", id='" + id + '\'' +
                ", getProfile='" + profile + '\'' +
                ", currentStep='" + currentStep + '\'' +
                ", remaining='" + remaining + '\'' +
                ", mission='" + mission + '\'' +
                '}';
    }

}
