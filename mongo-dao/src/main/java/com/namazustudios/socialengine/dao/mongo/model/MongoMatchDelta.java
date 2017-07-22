package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.model.TimeDelta;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by patricktwohig on 7/21/17.
 */
@Entity(value = "match_time_delta", noClassnameStored = true)
public class MongoMatchDelta {

    @Id
    private ObjectId matchId;

    @Indexed
    @Property
    private int sequence;

    @Indexed
    @Property
    private long timeStamp;

    @Property
    private ObjectId objectId;

    @Property
    private TimeDelta.Operation operation;

    @Embedded
    private MongoMatch snapshot;

    public ObjectId getMatchId() {
        return matchId;
    }

    public void setMatchId(ObjectId matchId) {
        this.matchId = matchId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public TimeDelta.Operation getOperation() {
        return operation;
    }

    public void setOperation(TimeDelta.Operation operation) {
        this.operation = operation;
    }

    public MongoMatch getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(MongoMatch snapshot) {
        this.snapshot = snapshot;
    }

    @PostLoad
    public void syncSnapshot() {
        if (getSnapshot() != null) {
            getSnapshot().setObjectId(getMatchId());
        }
    }

}
