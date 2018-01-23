package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

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
        if (!(o instanceof MongoApplication)) return false;

        MongoApplication that = (MongoApplication) o;

        if (isActive() != that.isActive()) return false;
        if (getObjectId() != null ? !getObjectId().equals(that.getObjectId()) : that.getObjectId() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        return getDescription() != null ? getDescription().equals(that.getDescription()) : that.getDescription() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (isActive() ? 1 : 0);
        return result;
    }
}
