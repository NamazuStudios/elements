package dev.getelements.elements.dao.mongo.model.auth;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;

import java.sql.Timestamp;

/**
 * MongoDB entity for a single-use "password verified, awaiting TOTP code" login challenge. The opaque
 * {@code id} doubles as the document {@code _id}, mirroring {@code MongoPasswordResetToken}. The TTL index on
 * {@code expiry} (with {@code expireAfterSeconds=0}) causes MongoDB to automatically remove expired documents.
 */
@Entity(value = "totp_login_challenge", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = 0))
})
public class MongoTotpLoginChallenge {

    @Id
    private String id;

    @Property
    private String userId;

    @Property
    private String profileId;

    @Property
    private String profileSelector;

    @Property
    private String applicationNameOrId;

    @Property
    private Timestamp expiry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileSelector() {
        return profileSelector;
    }

    public void setProfileSelector(String profileSelector) {
        this.profileSelector = profileSelector;
    }

    public String getApplicationNameOrId() {
        return applicationNameOrId;
    }

    public void setApplicationNameOrId(String applicationNameOrId) {
        this.applicationNameOrId = applicationNameOrId;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
