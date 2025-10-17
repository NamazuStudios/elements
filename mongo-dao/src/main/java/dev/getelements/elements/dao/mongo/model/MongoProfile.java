package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.largeobject.MongoLargeObject;
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
            @Field("active"),
            @Field("displayName")
        }
    ),
    @Index(fields = @Field(value = "displayName", type = IndexType.TEXT))
})
public class MongoProfile {

    @Id
    private ObjectId objectId;

    @Indexed
    @Property
    private boolean active;

    @Reference
    private MongoUser user;

    @Reference
    private MongoApplication application;

    @Property
    private String imageUrl;

    @Reference(ignoreMissing = true)
    private MongoLargeObject imageObject;

    @Indexed
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public MongoLargeObject getImageObject() {
        return imageObject;
    }

    public void setImageObject(MongoLargeObject imageObject) {
        this.imageObject = imageObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoProfile that = (MongoProfile) o;
        return isActive() == that.isActive() && Objects.equals(getObjectId(), that.getObjectId()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getApplication(), that.getApplication()) && Objects.equals(getImageUrl(), that.getImageUrl()) && Objects.equals(getImageObject(), that.getImageObject()) && Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getMetadata(), that.getMetadata()) && Objects.equals(getLastLogin(), that.getLastLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), isActive(), getUser(), getApplication(), getImageUrl(), getImageObject(), getDisplayName(), getMetadata(), getLastLogin());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoProfile{");
        sb.append("objectId=").append(objectId);
        sb.append(", active=").append(active);
        sb.append(", user=").append(user);
        sb.append(", application=").append(application);
        sb.append(", imageUrl='").append(imageUrl).append('\'');
        sb.append(", imageObject=").append(imageObject);
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", metadata=").append(metadata);
        sb.append(", lastLogin=").append(lastLogin);
        sb.append('}');
        return sb.toString();
    }

}
