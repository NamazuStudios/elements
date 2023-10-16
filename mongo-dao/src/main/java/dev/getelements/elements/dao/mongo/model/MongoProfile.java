package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;

/**
 * Created by patricktwohig on 6/28/17.
 */
@Entity(value = "profile", useDiscriminator = false)
@Indexes({
    @Index(fields = {
            @Field("user"),
            @Field("application"),
            @Field("active")
        }
    ),
    @Index(fields = @Field(value = "displayName", type = IndexType.TEXT))
})
public class MongoProfile {

    @Id
    private ObjectId objectId;

    @Property
    private boolean active;

    @Reference
    private MongoUser user;

    @Reference
    private MongoApplication application;

    @Reference
    private LargeObject largeObject;

    @Property
    private String displayName;

    @Property
    private Map<String, Object> metadata;

    @Indexed
    @Property
    private Timestamp lastLogin;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
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

    public MongoApplication getApplication() {
        return application;
    }

    public void setApplication(MongoApplication application) {
        this.application = application;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LargeObject getLargeObject() {
        return largeObject;
    }

    public void setLargeObject(LargeObject largeObject) {
        this.largeObject = largeObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoProfile that = (MongoProfile) o;
        return active == that.active && Objects.equals(objectId, that.objectId) && Objects.equals(user, that.user) && Objects.equals(application, that.application) && Objects.equals(largeObject, that.largeObject) && Objects.equals(displayName, that.displayName) && Objects.equals(metadata, that.metadata) && Objects.equals(lastLogin, that.lastLogin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, active, user, application, largeObject, displayName, metadata, lastLogin);
    }

    @Override
    public String toString() {
        return "MongoProfile{" +
                "objectId=" + objectId +
                ", active=" + active +
                ", user=" + user +
                ", application=" + application +
                ", largeObject=" + largeObject +
                ", displayName='" + displayName + '\'' +
                ", metadata=" + metadata +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
