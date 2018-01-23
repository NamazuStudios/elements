package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.model.TimeDelta;
import com.namazustudios.socialengine.model.match.Match;
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
    @Index(fields = @Field(value = "_id.timeStamp")),
    @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = MongoMatch.MATCH_EXPIRATION_SECONDS))
})
public class MongoMatchDelta {

    @Id
    private Key key;

    @Property
    private TimeDelta.Operation operation;

    @Property
    private Timestamp expiry;

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
     * The expiry property is set to null until the {@link MongoMatchDelta} is ready to be destroyed.  Such as when
     * the associated {@link MongoMatch} has been finalized or deleted.  The {@link MongoMatchDelta} will linger
     * in the database for the amount of time after this date as specified by {@link MongoMatch#MATCH_EXPIRATION_SECONDS}.
     *
     * @return the expiry.
     */
    public Timestamp getExpiry() {
        return expiry;
    }

    /**
     * Sets the expiry time.  This should be set to the current time to ensure the {@link MongoMatchDelta} expires
     * at the expected time.
     *
     * @param expiry the expiry time
     */
    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoMatchDelta)) return false;

        MongoMatchDelta that = (MongoMatchDelta) o;

        if (getKey() != null ? !getKey().equals(that.getKey()) : that.getKey() != null) return false;
        if (getOperation() != that.getOperation()) return false;
        if (getExpiry() != null ? !getExpiry().equals(that.getExpiry()) : that.getExpiry() != null) return false;
        return getSnapshot() != null ? getSnapshot().equals(that.getSnapshot()) : that.getSnapshot() == null;
    }

    @Override
    public int hashCode() {
        int result = getKey() != null ? getKey().hashCode() : 0;
        result = 31 * result + (getOperation() != null ? getOperation().hashCode() : 0);
        result = 31 * result + (getExpiry() != null ? getExpiry().hashCode() : 0);
        result = 31 * result + (getSnapshot() != null ? getSnapshot().hashCode() : 0);
        return result;
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
