package dev.getelements.elements.dao.mongo.model.goods;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import dev.getelements.elements.dao.mongo.model.ObjectIdExtractor;
import dev.getelements.elements.dao.mongo.model.ObjectIdProcessor;
import dev.getelements.elements.model.goods.ItemCategory;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.util.*;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoItem mongoItem = (MongoItem) o;
        return Objects.equals(getObjectId(), mongoItem.getObjectId()) && Objects.equals(getName(), mongoItem.getName()) && Objects.equals(getDisplayName(), mongoItem.getDisplayName()) && Objects.equals(getDescription(), mongoItem.getDescription()) && Objects.equals(getMetadata(), mongoItem.getMetadata()) && Objects.equals(getTags(), mongoItem.getTags()) && getCategory() == mongoItem.getCategory();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getName(), getDisplayName(), getDescription(), getMetadata(), getTags(), getCategory());
    }

}
