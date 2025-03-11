package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.leaderboard.Leaderboard.ScoreStrategyType;
import static dev.getelements.elements.sdk.model.leaderboard.Leaderboard.TimeStrategyType;
import static java.lang.System.currentTimeMillis;

@Entity(value = "leaderboard", useDiscriminator = false)
@Indexes({
    @Index(fields = @Field(value = "name"), options = @IndexOptions(unique = true))
})
public class MongoLeaderboard {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private TimeStrategyType timeStrategyType;

    @Property
    private ScoreStrategyType scoreStrategyType;

    @Property
    private String title;

    @Property
    private String scoreUnits;

    @Property
    private Timestamp firstEpochTimestamp;

    @Property
    private long epochInterval;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimeStrategyType getTimeStrategyType() {
        return timeStrategyType;
    }

    public void setTimeStrategyType(TimeStrategyType timeStrategyType) {
        this.timeStrategyType = timeStrategyType;
    }

    public ScoreStrategyType getScoreStrategyType() {
        return scoreStrategyType;
    }

    public void setScoreStrategyType(ScoreStrategyType scoreStrategyType) {
        this.scoreStrategyType = scoreStrategyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScoreUnits() {
        return scoreUnits;
    }

    public void setScoreUnits(String scoreUnits) {
        this.scoreUnits = scoreUnits;
    }

    public Timestamp getFirstEpochTimestamp() { return firstEpochTimestamp; }

    public void setFirstEpochTimestamp(Timestamp firstEpochTimestamp) { this.firstEpochTimestamp = firstEpochTimestamp; }

    public long getEpochInterval() { return epochInterval; }

    public void setEpochInterval(long epochInterval) { this.epochInterval = epochInterval; }

    /**
     * Whether or not the leaderboard has started its first epoch yet. If the leaderboard is not epochal, this will
     * always return false.
     *
     * @return whether or not the leaderboard has started its first epoch yet.
     */
    public boolean hasStarted() {
        if (this.timeStrategyType != TimeStrategyType.EPOCHAL) {
            return false;
        }

        long now = currentTimeMillis();
        long firstEpochMillis = firstEpochTimestamp.getTime();

        if (firstEpochMillis > now) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Calculates the epoch's starting millis to which the given millis timestamp belongs.
     *
     * @param millis the timestamp, in milliseconds, which is being looked up.
     * @return the epoch in millis if an epochal leaderboard and valid input, {@link MongoScoreId#ALL_TIME_LEADERBOARD_EPOCH}
     * if a global leaderboard, -1L if invalid input (i.e. the given millis occur before the firstEpochTimestamp).
     */
    public long calculateEpochForMillis(long millis) {
        if (this.timeStrategyType != TimeStrategyType.EPOCHAL) {
            return MongoScoreId.ALL_TIME_LEADERBOARD_EPOCH;
        }

        long firstEpochMillis = firstEpochTimestamp.getTime();

        if (millis < firstEpochMillis) {
            return -1L;
        }

        long timespanMillis = millis - firstEpochMillis;
        long epochCount = timespanMillis / epochInterval;
        long epochMillis = firstEpochMillis + epochCount * epochInterval;

        return epochMillis;
    }

    /**
     * Calculates the epoch for the current server time.
     *
     * @return the epoch in millis if an epochal leaderboard and valid input, {@link MongoScoreId#ALL_TIME_LEADERBOARD_EPOCH}
     * if a global leaderboard.
     */
    public long calculateCurrentEpoch() {
        long millis = currentTimeMillis();
        return calculateEpochForMillis(millis);
    }

    /**
     * Calculates the epoch's starting millis to which the given millis timestamp belongs.
     *
     * @param date the date being looked up.
     *
     * @return the epoch in millis if an epochal leaderboard and valid input, {@link MongoScoreId#ALL_TIME_LEADERBOARD_EPOCH}
     * if a global leaderboard, -1L if invalid input (i.e. the given date occurs before the firstEpochTimestamp).
     */
    public long getEpochForDate(Date date) {
        return this.calculateEpochForMillis(date.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoLeaderboard that = (MongoLeaderboard) o;
        return getEpochInterval() == that.getEpochInterval() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getName(), that.getName()) &&
                getTimeStrategyType() == that.getTimeStrategyType() &&
                getScoreStrategyType() == that.getScoreStrategyType() &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getScoreUnits(), that.getScoreUnits()) &&
                Objects.equals(getFirstEpochTimestamp(), that.getFirstEpochTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getName(), getTimeStrategyType(), getScoreStrategyType(), getTitle(),
                getScoreUnits(), getFirstEpochTimestamp(), getEpochInterval());
    }
}
