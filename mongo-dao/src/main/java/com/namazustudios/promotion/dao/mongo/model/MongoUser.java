package com.namazustudios.promotion.dao.mongo.model;

import com.namazustudios.promotion.model.User;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 3/31/15.
 */
@Entity(value = "user", noClassnameStored = true)
public class MongoUser {

    @Id
    private String objectId;

    @Property("name")
    @Indexed(unique=true)
    private String name;

    @Property("email")
    @Indexed(unique = true)
    private String email;

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
