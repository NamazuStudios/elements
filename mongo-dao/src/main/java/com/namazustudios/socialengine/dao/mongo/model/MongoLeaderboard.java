package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;

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
    private Timestamp dateStart;

    @Property
    private Long interval;

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

    public Timestamp getDateStart() { return dateStart; }

    public void setDateStart(Timestamp dateStart) { this.dateStart = dateStart; }

    public Long getInterval() { return interval; }

    public void setInterval(Long interval) { this.interval = interval; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoLeaderboard)) return false;

        MongoLeaderboard that = (MongoLeaderboard) o;

        if (getObjectId() != null ? !getObjectId().equals(that.getObjectId()) : that.getObjectId() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) return false;
        if (getDateStart() != null ? !getDateStart().equals(that.getDateStart()) : that.getDateStart() != null) return false;
        if (getInterval() != null ? !getInterval().equals(that.getInterval()) : that.getInterval() != null) return false;
        return getScoreUnits() != null ? getScoreUnits().equals(that.getScoreUnits()) : that.getScoreUnits() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getScoreUnits() != null ? getScoreUnits().hashCode() : 0);
        result = 31 * result + (getDateStart() != null ? getDateStart().hashCode() : 0);
        result = 31 * result + (getInterval() != null ? getInterval().hashCode() : 0);
        return result;
    }

}
