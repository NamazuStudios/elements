package dev.getelements.elements.dao.mongo.model.metadata;

import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Objects;

@Entity(value = "metadata", useDiscriminator = false)
public class MongoMetadata {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true, sparse = true))
    private String name;

    @Property
    private Map<String, Object> metadata;

    @Indexed
    @Reference
    private MongoMetadataSpec metadataSpec;

    @Property
    private User.Level accessLevel;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId id) {
        this.objectId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public MongoMetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    public void setMetadataSpec(MongoMetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
    }

    public User.Level getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(User.Level accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoMetadata that = (MongoMetadata) o;
        return Objects.equals(objectId, that.getObjectId()) && Objects.equals(name, that.getName()) && Objects.equals(metadata, that.getMetadata()) && Objects.equals(metadataSpec, that.getMetadataSpec()) && Objects.equals(accessLevel, that.getAccessLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, name, metadata, metadataSpec, accessLevel);
    }

    @Override
    public String toString() {
        return "CreateProfileRequest{" +
                "objectId='" + objectId + '\'' +
                ", name='" + name + '\'' +
                ", metadata='" + metadata + '\'' +
                ", spec='" + metadataSpec + '\'' +
                ", accessLevel=" + accessLevel +
                '}';
    }
}
