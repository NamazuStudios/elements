package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
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

    @Property
    private String imageUrl;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoProfile)) return false;
        MongoProfile that = (MongoProfile) object;
        return isActive() == that.isActive() &&
                Objects.equals(getLastLogin(), that.getLastLogin()) &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getApplication(), that.getApplication()) &&
                Objects.equals(getImageUrl(), that.getImageUrl()) &&
                Objects.equals(getDisplayName(), that.getDisplayName()) &&
                Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), isActive(), getUser(), getApplication(), getImageUrl(),
                getDisplayName(), getMetadata(), getLastLogin());
    }

}
