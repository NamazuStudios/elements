package com.namazustudios.socialengine.dao.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Entity(value = "application", noClassnameStored = true)
public class MongoApplication {

    @Id
    private String id;

    @Property("name")
    @Indexed(unique = true)
    private String name;

    @Property("description")
    private String description;

    @Indexed
    @Property("active")
    private boolean active;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

}
