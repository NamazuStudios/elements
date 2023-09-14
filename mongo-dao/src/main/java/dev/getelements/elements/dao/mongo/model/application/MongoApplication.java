package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Objects;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Entity(value = "application", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("name"), options = @IndexOptions(unique = true))
})
public class MongoApplication {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private String description;

    @Property
    private Map<String, Object> attributes;

    @Indexed
    @Property
    private boolean active;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoApplication that = (MongoApplication) o;
        return isActive() == that.isActive() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getName(), getDescription(), isActive());
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
