package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
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
@Entity(value = "profile", noClassnameStored = true)
@Indexes({
    @Index(fields = {@Field("user"), @Field("application")}, options = @IndexOptions(unique = true))
})
public class MongoProfile {

    @Id
    private ObjectId objectId;

    @Indexed
    @Property
    private boolean active;

    @Reference
    private MongoUser user;

    @Reference
    private MongoApplication application;

    @Property
    private String imageUrl;

    @Property
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

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof MongoProfile)) return false;

        MongoProfile that = (MongoProfile) o;

        if (isActive() != that.isActive()) return false;
        if (getObjectId() != null ? !getObjectId().equals(that.getObjectId()) : that.getObjectId() != null)
            return false;
        if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) return false;
        if (getApplication() != null ? !getApplication().equals(that.getApplication()) : that.getApplication() != null)
            return false;
        if (getImageUrl() != null ? !getImageUrl().equals(that.getImageUrl()) : that.getImageUrl() != null)
            return false;
        return getDisplayName() != null ? getDisplayName().equals(that.getDisplayName()) : that.getDisplayName() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (isActive() ? 1 : 0);
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getApplication() != null ? getApplication().hashCode() : 0);
        result = 31 * result + (getImageUrl() != null ? getImageUrl().hashCode() : 0);
        result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
        return result;
    }
}
