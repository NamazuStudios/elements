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

    @Property
    private MongoProgressMissionInfo mission;

    @Property
    private int sequence;

    @Property
    private int remaining;

    @Reference
    private List<MongoSchedule> schedules;

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

        final MongoProgressMissionInfo mission = getMission();
        final List<MongoStep> mongoSteps = mission.getSteps();

        return (mongoSteps == null || sequence >= mongoSteps.size()) ?
                mission.getFinalRepeatStep() :
                mongoSteps.get(sequence);

    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public MongoProgressMissionInfo getMission() {
        return mission;
    }

    public void setMission(MongoProgressMissionInfo mission) {
        this.mission = mission;
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

    public List<MongoSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<MongoSchedule> schedules) {
        this.schedules = schedules;
    }

    @PostLoad
    public void clearNulls() {
        if (rewardIssuances != null) {
            rewardIssuances = rewardIssuances.stream().filter(Objects::nonNull).collect(toList());
        }
    }

}
