package dev.getelements.elements.dao.mongo.model.largeobject;

import dev.getelements.elements.sdk.model.largeobject.AccessPermissions;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectState;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Objects;

@Entity(value = "large_object", useDiscriminator = false)
@Indexes({
        //TODO: check this when searching will be clear
        @Index(fields = {@Field("url"), @Field("path")})
})
public class MongoLargeObject {

    @Id
    private ObjectId id;

    @Property
    private String url;

    @Property
    private String path;

    @Property
    private String mimeType;

    @Property
    private LargeObjectState state;

    @Property
    private Date lastModified;

    @Property
    private AccessPermissions accessPermissions;

    @Property
    private String originalFilename;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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

    public LargeObjectState getState() {
        return state;
    }

    public void setState(LargeObjectState state) {
        this.state = state;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoLargeObject that = (MongoLargeObject) o;
        return Objects.equals(id, that.id) && Objects.equals(url, that.url) && Objects.equals(path, that.path) && Objects.equals(mimeType, that.mimeType) && state == that.state && Objects.equals(lastModified, that.lastModified) && Objects.equals(accessPermissions, that.accessPermissions) && Objects.equals(originalFilename, that.originalFilename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, path, mimeType, state, lastModified, accessPermissions, originalFilename);
    }

    @Override
    public String toString() {
        return "MongoLargeObject{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", path='" + path + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", state=" + state +
                ", lastModified=" + lastModified +
                ", accessPermissions=" + accessPermissions +
                ", originalFilename='" + originalFilename + '\'' +
                '}';
    }
}
