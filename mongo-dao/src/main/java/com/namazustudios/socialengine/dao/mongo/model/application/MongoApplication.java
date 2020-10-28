package com.namazustudios.socialengine.dao.mongo.model.application;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.util.Objects;

/**
 * Created by patricktwohig on 7/10/15.
 */

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class)
)
@SearchableDocument(
        fields = {
                @SearchableField(name = "name", path = "/name"),
                @SearchableField(name = "description", path = "/description"),
                @SearchableField(name = "active", path = "/active")
        }
)
@Entity(value = "application", noClassnameStored = true)
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
}
