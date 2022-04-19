package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.model.user.User;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import static dev.morphia.utils.IndexType.TEXT;

/**
 * Created by patricktwohig on 3/31/15.
 */
@SearchableIdentity(@SearchableField(
    name = "id",
    path = "/objectId",
    type = ObjectId.class,
    extractor = ObjectIdExtractor.class,
    processors = ObjectIdProcessor.class))
@SearchableDocument(fields = {
    @SearchableField(name = "name", path = "/name"),
    @SearchableField(name = "email", path = "/email"),
    @SearchableField(name = "active", path = "/active"),
    @SearchableField(name = "level", path = "/level"),
    @SearchableField(name = "facebookId", path = "/facebookId"),
    @SearchableField(name = "appleSignInId", path = "/appleSignInId")
})
@Entity(value = "user", useDiscriminator = false)
@Indexes({
    @Index(
        fields = {
            @Field(value = "name", type = TEXT),
            @Field(value = "email", type = TEXT)
        }
    ),
    @Index(
        fields = @Field(value = "name"),
        options = @IndexOptions(unique = true, collation = @Collation(locale = "en_US"))
    ),
    @Index(
        fields = @Field(value = "email"),
        options = @IndexOptions(unique = true, collation = @Collation(locale = "en_US"))
    ),
    @Index(
        fields = @Field(value = "firebaseId"),
        options = @IndexOptions(unique = true, sparse = true)
    ),
    @Index(
        fields = @Field(value = "facebookId"),
        options = @IndexOptions(unique = true, sparse = true)
    ),
    @Index(
        fields = @Field(value = "appleSignInId"),
        options = @IndexOptions(unique = true, sparse = true)
    ),
    @Index(
        fields = @Field(value = "externalUserId"),
        options = @IndexOptions(unique = true, sparse = true)
    )

})
public class MongoUser {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private String email;

    @Property
    private String hashAlgorithm;

    @Property
    private byte[] salt;

    @Property
    private byte[] passwordHash;

    @Property
    private User.Level level;

    @Indexed
    @Property
    private boolean active;

    @Property
    private String firebaseId;

    @Property
    private String facebookId;

    @Property
    private String appleSignInId;

    @Property
    private String externalUserId;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public User.Level getLevel() {
        return level;
    }

    public void setLevel(User.Level level) {
        this.level = level;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getAppleSignInId() {
        return appleSignInId;
    }

    public void setAppleSignInId(String appleSignInId) {
        this.appleSignInId = appleSignInId;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoUser mongoUser = (MongoUser) o;
        return isActive() == mongoUser.isActive() && Objects.equals(getObjectId(), mongoUser.getObjectId()) && Objects.equals(getName(), mongoUser.getName()) && Objects.equals(getEmail(), mongoUser.getEmail()) && Objects.equals(getHashAlgorithm(), mongoUser.getHashAlgorithm()) && Arrays.equals(getSalt(), mongoUser.getSalt()) && Arrays.equals(getPasswordHash(), mongoUser.getPasswordHash()) && getLevel() == mongoUser.getLevel() && Objects.equals(getFirebaseId(), mongoUser.getFirebaseId()) && Objects.equals(getFacebookId(), mongoUser.getFacebookId()) && Objects.equals(getAppleSignInId(), mongoUser.getAppleSignInId()) && Objects.equals(getExternalUserId(), mongoUser.getExternalUserId());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getObjectId(), getName(), getEmail(), getHashAlgorithm(), getLevel(), isActive(), getFirebaseId(), getFacebookId(), getAppleSignInId(), getExternalUserId());
        result = 31 * result + Arrays.hashCode(getSalt());
        result = 31 * result + Arrays.hashCode(getPasswordHash());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoUser{");
        sb.append("objectId=").append(objectId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", hashAlgorithm='").append(hashAlgorithm).append('\'');
        sb.append(", salt=").append(Arrays.toString(salt));
        sb.append(", passwordHash=").append(Arrays.toString(passwordHash));
        sb.append(", level=").append(level);
        sb.append(", active=").append(active);
        sb.append(", firebaseId='").append(firebaseId).append('\'');
        sb.append(", facebookId='").append(facebookId).append('\'');
        sb.append(", appleSignInId='").append(appleSignInId).append('\'');
        sb.append(", externalUserId='").append(externalUserId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
