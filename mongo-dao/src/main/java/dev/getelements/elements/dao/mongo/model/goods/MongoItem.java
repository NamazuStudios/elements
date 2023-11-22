package dev.getelements.elements.dao.mongo.model.goods;

import dev.getelements.elements.model.goods.ItemCategory;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.*;


@Entity(value = "items", useDiscriminator = false)
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
    private List<String> tags = new ArrayList<>();

    @Property
    private ItemCategory category;

    @Property
    private boolean isPublic;

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoItem mongoItem = (MongoItem) o;
        return isPublic == mongoItem.isPublic && Objects.equals(objectId, mongoItem.objectId) && Objects.equals(name, mongoItem.name) && Objects.equals(displayName, mongoItem.displayName) && Objects.equals(description, mongoItem.description) && Objects.equals(metadata, mongoItem.metadata) && Objects.equals(tags, mongoItem.tags) && category == mongoItem.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, name, displayName, description, metadata, tags, category, isPublic);
    }
}
