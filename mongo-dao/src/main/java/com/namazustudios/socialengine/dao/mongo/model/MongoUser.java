package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.model.user.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Arrays;
import java.util.Objects;

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
@Entity(value = "user", noClassnameStored = true)
public class MongoUser {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true))
    private String name;

    @Property
    @Indexed(options = @IndexOptions(unique = true))
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
    @Indexed(options = @IndexOptions(unique = true, sparse = true))
    private String facebookId;

    @Property
    @Indexed(options = @IndexOptions(unique = true, sparse = true))
    private String appleSignInId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoUser mongoUser = (MongoUser) o;
        return isActive() == mongoUser.isActive() &&
                Objects.equals(getObjectId(), mongoUser.getObjectId()) &&
                Objects.equals(getName(), mongoUser.getName()) &&
                Objects.equals(getEmail(), mongoUser.getEmail()) &&
                Objects.equals(getHashAlgorithm(), mongoUser.getHashAlgorithm()) &&
                Arrays.equals(getSalt(), mongoUser.getSalt()) &&
                Arrays.equals(getPasswordHash(), mongoUser.getPasswordHash()) &&
                getLevel() == mongoUser.getLevel() &&
                Objects.equals(getFacebookId(), mongoUser.getFacebookId()) &&
                Objects.equals(getAppleSignInId(), mongoUser.getAppleSignInId());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getObjectId(), getName(), getEmail(), getHashAlgorithm(), getLevel(), isActive(), getFacebookId(), getAppleSignInId());
        result = 31 * result + Arrays.hashCode(getSalt());
        result = 31 * result + Arrays.hashCode(getPasswordHash());
        return result;
    }

    @Override
    public String toString() {
        return "MongoUser{" +
                "objectId=" + objectId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", hashAlgorithm='" + hashAlgorithm + '\'' +
                ", salt=" + Arrays.toString(salt) +
                ", passwordHash=" + Arrays.toString(passwordHash) +
                ", level=" + level +
                ", active=" + active +
                ", facebookId='" + facebookId + '\'' +
                '}';
    }

}
