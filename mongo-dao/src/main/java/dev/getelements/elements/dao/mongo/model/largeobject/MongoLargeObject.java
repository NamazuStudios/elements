package dev.getelements.elements.dao.mongo.model.largeobject;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Objects;

@Entity(value = "large_object", useDiscriminator = false)
@Indexes({
        //TODO: check this when searching will be clear
        @Index(fields = {@Field("url"), @Field("path")})
})
public class MongoLargeObject {
    @Id
    private ObjectId objectId;

    @Property
    private String url;

    @Property
    private String path;

    @Property
    private String mimeType;

    @Property
    private AccessPermissions accessPermissions;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public AccessPermissions getAccessPermissions() {
        return accessPermissions;
    }

    public void setAccessPermissions(AccessPermissions accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoLargeObject that = (MongoLargeObject) o;
        return Objects.equals(objectId, that.objectId) && Objects.equals(url, that.url) && Objects.equals(path, that.path) && Objects.equals(mimeType, that.mimeType) && Objects.equals(accessPermissions, that.accessPermissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, url, path, mimeType, accessPermissions);
    }
}
