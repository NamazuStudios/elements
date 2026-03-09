package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Represents the mission state (i.e., progress).
 *
 * Created by davidjbrooks on 12/03/2018.
 */
public class Progress implements Serializable {

    /** Creates a new instance. */
    public Progress() {}

    @NotNull(groups={Update.class})
    @Null(groups={Create.class, Insert.class})
    @Schema(description = "The unique ID of the progress instance")
    private String id;

    @NotNull(groups={Create.class, Insert.class, Update.class})
    @Schema(description = "The profile of the owner of this progress")
    private Profile profile;

    @Null
    @Schema(description = "The current step")
    private Step currentStep;

    @Null
    @Schema(description = "The remaining actions")
    private Integer remaining;

    @NotNull(groups={Create.class, Insert.class, Update.class})
    @Schema(description = "The mission")
    private ProgressMissionInfo mission;

    @Schema(description = "List of all reward issuances that are issued but not expired, or redeemed but persistent.")
    private List<RewardIssuance> rewardIssuances;

    @Schema(description = "The current number of completed steps. Note that this may exceed the total number of steps, " +
            "i.e. the final step may be repeated infinitely.")
    private Integer sequence;

    @Schema(description = "Indicates that this progress is managed by a Schedule. If true, the Progress will be deleted " +
            "when no schedules have the progress active. This will be true if the Progress was created as part of a " +
            "Schedule.")
    private boolean managedBySchedule;

    @Schema(description = "A listing of the Schedules which are managing this Progress. Empty or null if the Progress " +
            "is not managed as part of a Schedule.")
    private List<Schedule> schedules;

    @Schema(description = "A listing of ScheduleEvents which are managing this Progress. Empty or null if the Progress " +
            "is not managed as part of a Schedule.")
    private List<ScheduleEvent> scheduleEvents;

    /**
     * Returns the unique ID of this progress instance.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of this progress instance.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the profile of the owner of this progress.
     *
     * @return the profile
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * Sets the profile of the owner of this progress.
     *
     * @param profile the profile
     */
    public void setProfile(Profile profile) { this.profile = profile; }

    /**
     * Returns the current mission step.
     *
     * @return the current step
     */
    public Step getCurrentStep() {
        return currentStep;
    }

    /**
     * Sets the current mission step.
     *
     * @param currentStep the current step
     */
    public void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Returns the number of remaining actions to complete the current step.
     *
     * @return the remaining count
     */
    public Integer getRemaining() {
        return remaining;
    }

    /**
     * Sets the number of remaining actions to complete the current step.
     *
     * @param remaining the remaining count
     */
    public void setRemaining(Integer remaining) { this.remaining = remaining; }

    /**
     * Returns the mission information for this progress.
     *
     * @return the mission info
     */
    public ProgressMissionInfo getMission() {
        return mission;
    }

    /**
     * Sets the mission information for this progress.
     *
     * @param mission the mission info
     */
    public void setMission(ProgressMissionInfo mission) {
        this.mission = mission;
    }

    /**
     * Returns the list of reward issuances associated with this progress.
     *
     * @return the reward issuances
     */
    public List<RewardIssuance> getRewardIssuances() {
        return rewardIssuances;
    }

    /**
     * Sets the list of reward issuances associated with this progress.
     *
     * @param rewardIssuances the reward issuances
     */
    public void setRewardIssuances(List<RewardIssuance> rewardIssuances) {
        this.rewardIssuances = rewardIssuances;
    }

    /**
     * Returns the current number of completed steps.
     *
     * @return the sequence
     */
    public Integer getSequence() {
        return sequence;
    }

    /**
     * Sets the current number of completed steps.
     *
     * @param sequence the sequence
     */
    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns whether this progress is managed by a Schedule.
     *
     * @return true if managed by a schedule
     */
    public boolean isManagedBySchedule() {
        return managedBySchedule;
    }

    /**
     * Sets whether this progress is managed by a Schedule.
     *
     * @param managedBySchedule true if managed by a schedule
     */
    public void setManagedBySchedule(boolean managedBySchedule) {
        this.managedBySchedule = managedBySchedule;
    }

    /**
     * Returns the list of Schedules managing this Progress.
     *
     * @return the schedules
     */
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Sets the list of Schedules managing this Progress.
     *
     * @param schedules the schedules
     */
    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    /**
     * Returns the list of ScheduleEvents managing this Progress.
     *
     * @return the schedule events
     */
    public List<ScheduleEvent> getScheduleEvents() {
        return scheduleEvents;
    }

    /**
     * Sets the list of ScheduleEvents managing this Progress.
     *
     * @param scheduleEvents the schedule events
     */
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
