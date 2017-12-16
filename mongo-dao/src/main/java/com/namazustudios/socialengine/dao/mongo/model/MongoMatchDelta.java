package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.model.TimeDelta;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.sql.Timestamp;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;

/**
 * Created by patricktwohig on 7/21/17.
 */
@Entity(value = "match_time_delta", noClassnameStored = true)
@Indexes({
    @Index(fields = @Field(value = "_id.match")),
    @Index(fields = @Field(value = "_id.sequence", type = IndexType.ASC)),
    @Index(fields = @Field(value = "_id.sequence", type = IndexType.DESC)),
    @Index(fields = @Field(value = "_id.timeStamp"), options = @IndexOptions(expireAfterSeconds = MongoMatchDelta.MATCH_DELTA_EXPIRATION_SECONDS))
})
public class MongoMatchDelta {

    /**
     * The amount of seconds a delta will be available.  This is set to something considerably
     * larger than what will ever be practically necessary, but allows clients to read a history
     * of the associated {@link MongoMatch} in order to sync state appropriately.  In reality
     * a {@link MongoMatchDelta} should only need to live for a few seconds.
     */
    public static final int MATCH_DELTA_EXPIRATION_SECONDS = 5 * 60;

    @Id
    private Key key;

    @Property
    private TimeDelta.Operation operation;

    @Embedded
    private MongoMatchSnapshot snapshot;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public TimeDelta.Operation getOperation() {
        return operation;
    }

    public void setOperation(TimeDelta.Operation operation) {
        this.operation = operation;
    }

    public MongoMatchSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(MongoMatchSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * A compound key type used to ensure only one delta per sequence and timestamp exists.
     * This supports the ability to easily and quickly increment the existing sequence number
     * and move to the next {@link MongoMatchDelta} in the sequence.
     */
    public static final class Key {

        @Property
        private ObjectId match;

        @Property
        private int sequence;

        @Property
        private Timestamp timeStamp;

        public Key() {}

        public Key(MongoMatch match) {
            this(match.getObjectId(), 0, match.getLastUpdatedTimestamp().getTime());
        }

        public Key(ObjectId match) {
            this(match, 0, currentTimeMillis());
        }

        public Key(ObjectId match, int sequence, long timeStamp) {
            this(match, sequence, new Timestamp(timeStamp));
        }

        public Key(ObjectId match, int sequence, Timestamp timeStamp) {
            this.match = match;
            this.sequence = sequence;
            this.timeStamp = timeStamp;
        }

        public ObjectId getMatch() {
            return match;
        }

        public int getSequence() {
            return sequence;
        }

        public Timestamp getTimeStamp() {
            return timeStamp;
        }

        /**
         * Generates the next {@link Key} in the sequence.
         *
         * @return the next {@link Key}
         */
        public Key nextInSequence() {
            // A poor-man's method of compensating for clock-skew.  This is probably not that accruate
            // but for our purposes will have to do.  If separate hosts write diffs and the clocks are
            // slightly off this will just adjust to ensure that the write happens at the same time because
            // the sequence (which always moves forward) will ultimately be the tiebreaker.
            final long timeStamp = max(currentTimeMillis(), getTimeStamp().getTime());
            return new Key(getMatch(), getSequence() + 1, timeStamp);
        }

        /**
         * Generates the next {@link Key} in the sequence.
         *
         * @return the next {@link Key}
         */
        public Key nextInSequence(long timeStamp) {
            // A poor-man's method of compensating for clock-skew.  This is probably not that accruate
            // but for our purposes will have to do.  If separate hosts write diffs and the clocks are
            // slightly off this will just adjust to ensure that the write happens at the same time because
            // the sequence (which always moves forward) will ultimately be the tiebreaker.
            return new Key(getMatch(), getSequence() + 1, max(timeStamp, getTimeStamp().getTime()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;

            Key key = (Key) o;

            if (getSequence() != key.getSequence()) return false;
            if (getMatch() != null ? !getMatch().equals(key.getMatch()) : key.getMatch() != null) return false;
            return getTimeStamp() != null ? getTimeStamp().equals(key.getTimeStamp()) : key.getTimeStamp() == null;
        }

        @Override
        public int hashCode() {
            int result = getMatch() != null ? getMatch().hashCode() : 0;
            result = 31 * result + getSequence();
            result = 31 * result + (getTimeStamp() != null ? getTimeStamp().hashCode() : 0);
            return result;
        }

    }

}
