package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.model.User;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 3/31/15.
 */
@Entity(value = "user", noClassnameStored = true)

@SearchableIdentity(@SearchableField(name = "id", path = "/objectId"))
@SearchableDocument(
        fields = {
            @SearchableField(name = "name", path = "/name"),
            @SearchableField(name = "email", path = "/email"),
            @SearchableField(name = "active", path = "/active"),
            @SearchableField(name = "level", path = "/level")
        }
)
public class MongoUser {

    @Id
    private String objectId;

    @Property("name")
    @Indexed(unique=true)
    private String name;

    @Property("email")
    @Indexed(unique = true)
    private String email;

    @Property("hash_algorithm")
    private String hashAlgorithm;

    @Property("salt")
    private byte[] salt;

    @Property("password_hash")
    private byte[] passwordHash;

    @Property("level")
    private User.Level level;

    @Property("active")
    @Indexed
    private boolean active;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
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

}
