package com.namazustudios.socialengine.dao.mongo.model;

        import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
        import com.namazustudios.socialengine.fts.annotation.SearchableField;
        import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
        import com.namazustudios.socialengine.model.application.Platform;
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
                @SearchableField(name = "platform", path = "/platform"),
                @SearchableField(name = "active", path = "/active")
        }
)
@Indexes({
        @Index(value = "platform, parent, name", unique = true),
        @Index(value = "platform"),
        @Index(value = "parent"),
        @Index(value = "name")
})
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoApplicationConfiguration {

    @Id
    private ObjectId objectId;

    @Property("name")
    private String uniqueIdentifier;

    @Reference("parent")
    private MongoApplication parent;

    @Property("platform")
    private Platform platform;

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

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
