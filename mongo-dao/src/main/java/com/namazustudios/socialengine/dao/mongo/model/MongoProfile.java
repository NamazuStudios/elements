package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by patricktwohig on 6/28/17.
 */
@SearchableIdentity(@SearchableField(
    name = "id",
    path = "/objectId",
    type = ObjectId.class,
    extractor = ObjectIdExtractor.class,
    processors = ObjectIdProcessor.class))
@SearchableDocument(
    fields = {
        @SearchableField(name = "userName",    path = "/user/name"),
        @SearchableField(name = "userEmail",   path = "/user/email"),
        @SearchableField(name = "displayName", path = "/displayName"),
        @SearchableField(name = "active", path = "/active")
    })
@Entity(value = "application", noClassnameStored = true)
@Indexes({
    @Index(fields = @Field("active")),
    @Index(fields = {@Field("user"), @Field("application")}, unique = true)
})
public class MongoProfile {

    @Id
    private ObjectId objectId;

    @Property
    private boolean active;

    @Reference
    private MongoUser user;

    @Reference
    private MongoApplication application;

    @Property("imageUrl")
    private String imageUrl;

    @Property("displayName")
    private String displayName;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoApplication getApplication() {
        return application;
    }

    public void setApplication(MongoApplication application) {
        this.application = application;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
