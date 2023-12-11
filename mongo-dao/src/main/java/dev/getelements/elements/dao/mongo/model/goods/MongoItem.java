package dev.getelements.elements.dao.mongo.model.goods;

import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
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

    @Reference
    private MongoMetadataSpec metadataSpec;

    @Property
    private Map<String, Object> metadata = new HashMap<>();

    @Indexed
    @Property
    private List<String> tags = new ArrayList<>();

    @Property
    private ItemCategory category;

    @Property
    private boolean publicVisible;

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

    public MongoMetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    public void setMetadataSpec(MongoMetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
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

    public boolean getPublicVisible() {
        return publicVisible;
    }

    public void setPublicVisible(boolean publicVisible) {
        this.publicVisible = publicVisible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoItem mongoItem = (MongoItem) o;
        return Objects.equals(objectId, mongoItem.objectId) && Objects.equals(name, mongoItem.name) && Objects.equals(displayName, mongoItem.displayName) && Objects.equals(description, mongoItem.description) && Objects.equals(metadataSpec, mongoItem.metadataSpec) && Objects.equals(metadata, mongoItem.metadata) && Objects.equals(tags, mongoItem.tags) && category == mongoItem.category && Objects.equals(publicVisible, mongoItem.publicVisible);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, name, displayName, description, metadataSpec, metadata, tags, category, publicVisible);
    }

    @Override
    public String toString() {
        return "MongoItem{" +
                "objectId=" + objectId +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", metadataSpec=" + metadataSpec +
                ", metadata=" + metadata +
                ", tags=" + tags +
                ", category=" + category +
                ", publicVisible=" + publicVisible +
                '}';
    }
}
