package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by patricktwohig on 7/10/15.
 */

@SearchableIdentity(@SearchableField(name = "id", path = "/objectId", type = String.class))
@SearchableDocument(
        fields = {
                @SearchableField(name = "name", path = "/name"),
                @SearchableField(name = "description", path = "/description"),
                @SearchableField(name = "active", path = "/active")
        }
)
@Entity(value = "application", noClassnameStored = true)
@Indexes({
    @Index(value = "name", unique = true),
    @Index(value = "active")
})
public class MongoApplication {

    @Id
    private ObjectId objectId;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Indexed
    @Property("active")
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

}
