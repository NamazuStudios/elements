package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.model.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity(value = "session", noClassnameStored = true)
@Indexes({
        @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = MongoSession.SESSION_EXPIRATION_SECONDS))
})
public class MongoSession {

    public static final int SESSION_EXPIRATION_SECONDS = 60 * 60 * 24;

    @Id
    private UUID sessionId;

    @Indexed
    @Reference
    private MongoUser user;

    @Reference
    private MongoProfile profile;

    @Reference
    private MongoApplication application;

    @Property
    private Timestamp expiry;

//    /**
//     * Gets this session's {@link ObjectId}.
//     * @return the {@link ObjectId}
//     */
//    public ObjectId getObjectId() {
//        return objectId;
//    }
//
//    /**
//     * Sets this session's {@link ObjectId}.
//     *
//     * @param objectId  the {@link ObjectId}
//     */
//    public void setObjectId(ObjectId objectId) {
//        this.objectId = objectId;
//    }

    /**
     * Gets the {@link MongoUser} which owns the {@link MongoSession}
     *
     * @return the {@link User}
     */
    public MongoUser getUser() {
        return user;
    }

    /**
     * Sets the {@link MongoUser} which owns the {@link MongoSession}
     *
     * @param user the {@link User}
     */
    public void setUser(MongoUser user) {
        this.user = user;
    }

    /**
     * Gets the {@link MongoProfile} associated with this {@link MongoSession}.
     *
     * @return the {@link MongoProfile}
     */
    public MongoProfile getProfile() {
        return profile;
    }

    /**
     * Sets the {@link MongoProfile} associated with this {@link MongoSession}.
     *
     * @param profile the {@link MongoProfile}
     */
    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    /**
     * Gets the {@link MongoApplication} associated with this {@link MongoSession}.
     *
     * @return the {@link MongoSession}
     */
    public MongoApplication getApplication() {
        return application;
    }

    /**
     * Sets the {@link MongoApplication} associated witht he {@link MongoSession}.
     *
     * @param application the {@link MongoApplication}
     */
    public void setApplication(MongoApplication application) {
        this.application = application;
    }

    /***
     * Gets the {@link Timestamp} at which this {@link MongoSession} expires.
     *
     * @return the {@link Timestamp} at which this {@link MongoSession} expires.
     */
    public Timestamp getExpiry() {
        return expiry;
    }

    /**
     * Sets th {@link Timestamp} at which the {@link MongoSession} expires.
     *
     * @param expiry the {@link Timestamp} at which the {@link MongoSession} expires.
     */
    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof MongoSession)) return false;
//
//        MongoSession that = (MongoSession) o;
//
//        if (objectId != null ? !objectId.equals(that.objectId) : that.objectId != null) return false;
//        if (user != null ? !user.equals(that.user) : that.user != null) return false;
//        if (profile != null ? !profile.equals(that.profile) : that.profile != null) return false;
//        return application != null ? application.equals(that.application) : that.application == null;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = objectId != null ? objectId.hashCode() : 0;
//        result = 31 * result + (user != null ? user.hashCode() : 0);
//        result = 31 * result + (profile != null ? profile.hashCode() : 0);
//        result = 31 * result + (application != null ? application.hashCode() : 0);
//        return result;
//    }

}
