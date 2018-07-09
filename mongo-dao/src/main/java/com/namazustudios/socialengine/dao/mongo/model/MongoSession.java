package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.model.User;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;

@Entity(value = "session", noClassnameStored = true)
@Indexes({
        @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = MongoSession.SESSION_LINGER_SECONDS))
})
public class MongoSession {

    public static final int SESSION_LINGER_SECONDS = 60 * 60 * 24;

    @Id
    private String sessionId;

    @Indexed
    @Reference
    private MongoUser user;

    @Reference
    private MongoProfile profile;

    @Reference
    private MongoApplication application;

    @Property
    private Timestamp expiry;

    /**
     * Gets the id of this {@link MongoSession}.
     *
     * @return the sessionId of this session
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the id of the {@link MongoSession}.
     *
     * @param sessionId the sesison id
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

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

}
