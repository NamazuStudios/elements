package dev.getelements.elements.dao.mongo.model.mission;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.sql.Timestamp;
import java.util.List;

@Entity("schedule_event")
public class MongoScheduleEvent {

    @Id
    private ObjectId objectId;

    @Indexed
    @Property
    private Timestamp begin;

    @Indexed
    @Property
    private Timestamp end;

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

    public Timestamp getBegin() {
        return begin;
    }

    public void setBegin(Timestamp begin) {
        this.begin = begin;
    }

    public Timestamp getEnd() {
        return end;
    }

    public void setEnd(Timestamp end) {
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
