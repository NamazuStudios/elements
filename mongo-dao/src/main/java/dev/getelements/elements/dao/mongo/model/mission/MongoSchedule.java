package dev.getelements.elements.dao.mongo.model.mission;

import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

public class MongoSchedule {


    @Id
    private ObjectId objectId;

    @Property
    private String name;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
