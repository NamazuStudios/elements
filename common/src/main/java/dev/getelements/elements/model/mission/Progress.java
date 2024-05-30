package dev.getelements.elements.model.mission;

import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Update;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.reward.RewardIssuance;
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

    @ApiModelProperty("The current number of completed steps. Note that this may exceed the total number of steps, " +
            "i.e. the final step may be repeated infinitely.")
    private Integer sequence;

    @ApiModelProperty("Indicates that this progress is managed by a Schedule. If true, the Progress will be deleted " +
            "when no schedules have the progress active. This will be true if the Progress was created as part of a " +
            "Schedule.")
    private boolean managedBySchedule;

    @ApiModelProperty("A listing of the Schedules which are managing this Progress. Empty or null if the Progress " +
            "is not managed as part of a Schedule.")
    private List<Schedule> schedules;

    @ApiModelProperty("A listing of ScheduleEvents which are managing this Progress. Empty or null if the Progress " +
            "is not managed as part of a Schedule.")
    private List<ScheduleEvent> scheduleEvents;

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

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public boolean isManagedBySchedule() {
        return managedBySchedule;
    }

    public void setManagedBySchedule(boolean managedBySchedule) {
        this.managedBySchedule = managedBySchedule;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public List<ScheduleEvent> getScheduleEvents() {
        return scheduleEvents;
    }

    public void setScheduleEvents(List<ScheduleEvent> scheduleEvents) {
        this.scheduleEvents = scheduleEvents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Progress progress = (Progress) o;
        return isManagedBySchedule() == progress.isManagedBySchedule() && Objects.equals(getId(), progress.getId()) && Objects.equals(getProfile(), progress.getProfile()) && Objects.equals(getCurrentStep(), progress.getCurrentStep()) && Objects.equals(getRemaining(), progress.getRemaining()) && Objects.equals(getMission(), progress.getMission()) && Objects.equals(getRewardIssuances(), progress.getRewardIssuances()) && Objects.equals(getSequence(), progress.getSequence()) && Objects.equals(getSchedules(), progress.getSchedules()) && Objects.equals(getScheduleEvents(), progress.getScheduleEvents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getProfile(), getCurrentStep(), getRemaining(), getMission(), getRewardIssuances(), getSequence(), isManagedBySchedule(), getSchedules(), getScheduleEvents());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Progress{");
        sb.append("id='").append(id).append('\'');
        sb.append(", profile=").append(profile);
        sb.append(", currentStep=").append(currentStep);
        sb.append(", remaining=").append(remaining);
        sb.append(", mission=").append(mission);
        sb.append(", rewardIssuances=").append(rewardIssuances);
        sb.append(", sequence=").append(sequence);
        sb.append(", managedBySchedule=").append(managedBySchedule);
        sb.append(", schedules=").append(schedules);
        sb.append(", scheduleEvents=").append(scheduleEvents);
        sb.append('}');
        return sb.toString();
    }

}
