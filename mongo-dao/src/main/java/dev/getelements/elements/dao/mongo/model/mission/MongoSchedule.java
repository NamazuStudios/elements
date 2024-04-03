package dev.getelements.elements.dao.mongo.model.mission;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;


@Entity(value = "mission", useDiscriminator = false)
public class MongoSchedule {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true, sparse = true))
    private String name;

    @Text
    @Property
    private String displayName;

    @Indexed
    @Property
    private String description;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
