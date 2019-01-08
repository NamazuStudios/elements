package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Date;

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
    private Long epochInterval;

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

    public Long getEpochInterval() { return epochInterval; }

    public void setEpochInterval(Long epochInterval) { this.epochInterval = epochInterval; }

    public Long getEpochForDate(Date date) {
        return this.getEpochForMillis(date.getTime());
    }

    // Calculates the epoch's starting millis to which the given millis timestamp belongs, defaults to 0.
    public Long getEpochForMillis(long millis) {
        if (firstEpochTimestamp == null || epochInterval == null) {
            return 0L;
        }

        long firstEpochMillis = firstEpochTimestamp.getTime();

        if (millis < firstEpochMillis) {
            return 0L;
        }

        long timespanMillis = millis - firstEpochMillis;
        long epochCount = timespanMillis / epochInterval;
        long epochMillis = firstEpochMillis + epochCount * epochInterval;

        return epochMillis;
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
        if (getEpochInterval() != null ? !getEpochInterval().equals(that.getEpochInterval()) : that.getEpochInterval() != null) return false;
        return getScoreUnits() != null ? getScoreUnits().equals(that.getScoreUnits()) : that.getScoreUnits() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getScoreUnits() != null ? getScoreUnits().hashCode() : 0);
        result = 31 * result + (getFirstEpochTimestamp() != null ? getFirstEpochTimestamp().hashCode() : 0);
        result = 31 * result + (getEpochInterval() != null ? getEpochInterval().hashCode() : 0);
        return result;
    }

}
