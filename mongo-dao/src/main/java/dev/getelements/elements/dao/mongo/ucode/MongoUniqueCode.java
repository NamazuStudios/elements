package dev.getelements.elements.dao.mongo.ucode;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.annotations.*;

import java.sql.Timestamp;

@Entity(value = "unique_codes", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("banned")),
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
    private boolean banned;

    @Property
    private Timestamp expiry;

    @Property
    private User user;

    @Property
    private Profile profile;

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

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
