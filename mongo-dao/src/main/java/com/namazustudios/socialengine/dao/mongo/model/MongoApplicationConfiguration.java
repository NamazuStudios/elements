package com.namazustudios.socialengine.dao.mongo.model;

        import com.namazustudios.elements.fts.annotation.SearchableDocument;
        import com.namazustudios.elements.fts.annotation.SearchableField;
        import com.namazustudios.elements.fts.annotation.SearchableIdentity;
        import com.namazustudios.socialengine.model.application.ConfigurationCategory;
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

}
