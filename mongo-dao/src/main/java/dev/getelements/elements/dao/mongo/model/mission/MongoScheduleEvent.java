package dev.getelements.elements.dao.mongo.model.mission;

import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

import java.util.List;

public class MongoScheduleEvent {

    @Id
    private ObjectId objectId;

    @Indexed
    @Property
    private Long begin;

    @Indexed
    @Property
    private Long end;

    @Indexed
    @Reference
    private MongoSchedule schedule;

    @Reference
    private List<MongoMission> missions;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public MongoSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(MongoSchedule schedule) {
        this.schedule = schedule;
    }

    public List<MongoMission> getMissions() {
        return missions;
    }

    public void setMissions(List<MongoMission> missions) {
        this.missions = missions;
    }

}
