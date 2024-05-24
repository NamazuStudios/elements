package dev.getelements.elements.dao.mongo.model.mission;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Mongo DTO for a mission progress.
 *
 * Created by davidjbrooks on 12/04/2018.
 */
@Entity(value = "progress", useDiscriminator = false)
public class MongoProgress {

    @Id
    private MongoProgressId objectId;

    @Indexed
    @Property
    private String version;

    @Indexed
    @Reference
    private MongoProfile profile;

    @Indexed
    @Reference
    private MongoMission mission;

    @Indexed
    private List<String> missionTags;

    @Property
    private int sequence;

    @Property
    private int remaining;

    @Property
    private boolean managedBySchedule;

    @Indexed
    @Reference
    private List<MongoSchedule> schedules;

    @Indexed
    @Reference
    private List<MongoScheduleEvent> scheduleEvents;

    @Reference(ignoreMissing = true)
    private List<MongoRewardIssuance> rewardIssuances;

    public MongoProgressId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoProgressId id) {
        this.objectId = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public MongoStep getCurrentStep() {
        final int sequence = getSequence();
        return getStepForSequence(sequence);
    }

    public MongoStep getStepForSequence(final int sequence) {

        final var mission = getMission();
        final var mongoSteps = mission == null ? null : mission.getSteps();
        final var finalRepeatStep = mission == null ? null : mission.getFinalRepeatStep();

        return (mongoSteps == null || sequence >= mongoSteps.size())
                ? finalRepeatStep
                : mongoSteps.get(sequence);

    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public MongoMission getMission() {
        return mission;
    }

    public void setMission(MongoMission mission) {
        this.mission = mission;
    }

    public List<String> getMissionTags() {
        return missionTags;
    }

    public void setMissionTags(List<String> missionTags) {
        this.missionTags = missionTags;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public List<MongoRewardIssuance> getRewardIssuances() {
        return rewardIssuances;
    }

    public void setRewardIssuances(List<MongoRewardIssuance> rewardIssuances) {
        this.rewardIssuances = rewardIssuances;
    }

    public boolean isManagedBySchedule() {
        return managedBySchedule;
    }

    public void setManagedBySchedule(boolean managedBySchedule) {
        this.managedBySchedule = managedBySchedule;
    }

    public List<MongoSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<MongoSchedule> schedules) {
        this.schedules = schedules;
    }

    public List<MongoScheduleEvent> getScheduleEvents() {
        return scheduleEvents;
    }

    public void setScheduleEvents(List<MongoScheduleEvent> scheduleEvents) {
        this.scheduleEvents = scheduleEvents;
    }

    @PostLoad
    public void clearNulls() {
        if (rewardIssuances != null) {
            rewardIssuances = rewardIssuances.stream().filter(Objects::nonNull).collect(toList());
        }
    }

}
