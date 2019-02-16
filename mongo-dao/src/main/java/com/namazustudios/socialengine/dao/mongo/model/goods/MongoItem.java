package com.namazustudios.socialengine.dao.mongo.model.goods;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@SearchableIdentity(@SearchableField(
    name = "id",
    path = "/objectId",
    type = ObjectId.class,
    extractor = ObjectIdExtractor.class,
    processors = ObjectIdProcessor.class))
@SearchableDocument(fields = {
    @SearchableField(name = "name", path = "/name"),
    @SearchableField(name = "displayName", path = "/displayName"),
    @SearchableField(name = "description", path = "/description"),
    @SearchableField(name = "tags", path = "/tags")
})
@Entity(value = "items", noClassnameStored = true)
public class MongoItem {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true))
    private String name;

    @Property
    private String displayName;

    @Property
    private String description;

    @Property
    private Map<String, Object> metadata = new HashMap<>();

    @Indexed
    @Property
    private Set<String> tags = new HashSet<>();

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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MongoItem mongoItem = (MongoItem) o;

        if (getObjectId() != null ? !getObjectId().equals(mongoItem.getObjectId()) : mongoItem.getObjectId() != null) {
            return false;
        }
        if (getName() != null ? !getName().equals(mongoItem.getName()) : mongoItem.getName() != null) {
            return false;
        }
        if (getDisplayName() != null ? !getDisplayName().equals(mongoItem.getDisplayName()) :
            mongoItem.getDisplayName() != null) {
            return false;
        }
        if (getDescription() != null ? !getDescription().equals(mongoItem.getDescription()) :
            mongoItem.getDescription() != null) {
            return false;
        }
        if (getMetadata() != null ? !getMetadata().equals(mongoItem.getMetadata()) : mongoItem.getMetadata() != null) {
            return false;
        }
        return getTags() != null ? getTags().equals(mongoItem.getTags()) : mongoItem.getTags() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getMetadata() != null ? getMetadata().hashCode() : 0);
        result = 31 * result + (getTags() != null ? getTags().hashCode() : 0);
        return result;
    }
}
