package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

@SearchableIdentity(@SearchableField(
    name = "id",
    path = "/objectId",
    type = ObjectId.class,
    extractor = ObjectIdExtractor.class,
    processors = ObjectIdProcessor.class))
@SearchableDocument(
    fields = {
        @SearchableField(name = "name", path = "/name"),
        @SearchableField(name = "title", path = "/title"),
        @SearchableField(name = "scoreUnits",  path = "/scoreUnits")
    })
@Entity(value = "leaderboard", noClassnameStored = true)
@Indexes({
    @Index(fields = @Field(value = "name"), options = @IndexOptions(unique = true))
})
public class MongoLeaderboard {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

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
     * Whether the leaderboard is epochal (if not, the leaderboard is considered all-time).
     *
     * @return whether or not the leaderboard is epochal.
     */
    public boolean isEpochal() {
        if (firstEpochTimestamp == null || firstEpochTimestamp.getTime() <= 0L || epochInterval <= 0L) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Whether or not the leaderboard has started its first epoch yet. If the leaderboard is not epochal, this will
     * always return false.
     * @return whether or not the leaderboard has started its first epoch yet.
     */
    public boolean hasStarted() {
        if (!isEpochal()) {
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
     * @return the epoch in millis if an epochal leaderboard and valid input, 0L if a global leaderboard, -1L if invalid
     * input.
     */
    public long getEpochForMillis(long millis) {
        if (!isEpochal()) {
            return 0L;
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
     * @return the epoch in millis if an epochal leaderboard and valid input, 0L if a global leaderboard, -1L if invalid
     * input.
     */
    public long getCurrentEpoch() {
        long millis = currentTimeMillis();
        return getEpochForMillis(millis);
    }

    /**
     * Calculates the epoch's starting millis to which the given millis timestamp belongs.
     *
     * @param date the date being looked up.
     * @return the epoch in millis if an epochal leaderboard and valid input, 0L if a global leaderboard, -1L if invalid
     * input.
     */
    public long getEpochForDate(Date date) {
        return this.getEpochForMillis(date.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoLeaderboard)) return false;

        MongoLeaderboard that = (MongoLeaderboard) o;

        if (getObjectId() != null ? !getObjectId().equals(that.getObjectId()) : that.getObjectId() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) return false;
        if (getFirstEpochTimestamp() != null ? !getFirstEpochTimestamp().equals(that.getFirstEpochTimestamp()) : that.getFirstEpochTimestamp() != null) return false;
        if (getEpochInterval() != that.getEpochInterval()) return false;
        return getScoreUnits() != null ? getScoreUnits().equals(that.getScoreUnits()) : that.getScoreUnits() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getScoreUnits() != null ? getScoreUnits().hashCode() : 0);
        result = 31 * result + (getFirstEpochTimestamp() != null ? getFirstEpochTimestamp().hashCode() : 0);
        result = 31 * result + (int) (getEpochInterval() ^ (getEpochInterval() >>> 32));
        return result;
    }

}
