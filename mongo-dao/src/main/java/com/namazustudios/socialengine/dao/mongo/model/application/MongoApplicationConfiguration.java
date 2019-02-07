package com.namazustudios.socialengine.dao.mongo.model.application;

        import com.namazustudios.elements.fts.annotation.SearchableDocument;
        import com.namazustudios.elements.fts.annotation.SearchableField;
        import com.namazustudios.elements.fts.annotation.SearchableIdentity;
        import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
        import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
        import com.namazustudios.socialengine.model.application.ConfigurationCategory;
        import org.bson.types.ObjectId;
        import org.mongodb.morphia.annotations.*;

        import java.util.HashMap;
        import java.util.Map;
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
                @SearchableField(name = "uniqueIdentifier", path = "/uniqueIdentifier"),
                @SearchableField(name = "applicationName", path = "/parent/name"),
                @SearchableField(name = "category", path = "/category"),
                @SearchableField(name = "active", path = "/active")
        }
)
@Indexes({
        @Index(fields = {@Field("category"), @Field("parent"), @Field("name") }, options = @IndexOptions(unique = true)),
})
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoApplicationConfiguration {

    @Id
    private ObjectId objectId;

    @Indexed
    @Property("name")
    private String uniqueIdentifier;

    @Indexed
    @Reference("parent")
    private MongoApplication parent;

    @Indexed
    @Property("category")
    private ConfigurationCategory category;

    @Indexed
    @Property("active")
    private boolean active;

    @Property
    private Map<String, String> iapProductIdsToItemIds = new HashMap<>();

    @Property
    private Map<String, Integer> iapProductIdsToRewardQuantities = new HashMap<>();

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public MongoApplication getParent() {
        return parent;
    }

    public void setParent(MongoApplication parent) {
        this.parent = parent;
    }

    public ConfigurationCategory getCategory() {
        return category;
    }

    public void setCategory(ConfigurationCategory category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, String> getIapProductIdsToItemIds() {
        return iapProductIdsToItemIds;
    }

    public void setIapProductIdsToItemIds(Map<String, String> iapProductIdsToItemIds) {
        this.iapProductIdsToItemIds = iapProductIdsToItemIds;
    }

    public Map<String, Integer> getIapProductIdsToRewardQuantities() {
        return iapProductIdsToRewardQuantities;
    }

    public void setIapProductIdsToRewardQuantities(Map<String, Integer> iapProductIdsToRewardQuantities) {
        this.iapProductIdsToRewardQuantities = iapProductIdsToRewardQuantities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoApplicationConfiguration that = (MongoApplicationConfiguration) o;
        return isActive() == that.isActive() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getUniqueIdentifier(), that.getUniqueIdentifier()) &&
                Objects.equals(getParent(), that.getParent()) &&
                getCategory() == that.getCategory() &&
                Objects.equals(getIapProductIdsToItemIds(), that.getIapProductIdsToItemIds()) &&
                Objects.equals(getIapProductIdsToRewardQuantities(), that.getIapProductIdsToRewardQuantities());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getUniqueIdentifier(), getParent(), getCategory(), isActive(),
                getIapProductIdsToItemIds(), getIapProductIdsToRewardQuantities());
    }
}
