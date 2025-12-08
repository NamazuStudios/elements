package dev.getelements.elements.dao.mongo.ucode;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.annotations.*;

import java.sql.Timestamp;

@Entity(value = "unique_codes", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("released")),
        @Index(fields = @Field("expiry"), options = @IndexOptions(expireAfterSeconds = 0))
})
public class MongoUniqueCode {

    @Id
    private String id;

    @Property
    private long linger;

    @Property
    private long timeout;

    @Property
    private Timestamp expiry;

    @Property
    private boolean active;

    @Property
    private MongoUser user;

    @Property
    private MongoProfile profile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLinger() {
        return linger;
    }

    public void setLinger(long linger) {
        this.linger = linger;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

}
