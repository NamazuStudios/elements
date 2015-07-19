package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.Platform;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by patricktwohig on 7/10/15.
 */
@SearchableIdentity(@SearchableField(name = "id", path = "/objectId", type = String.class))
@SearchableDocument(
        fields = {
                @SearchableField(name = "applicationId", path = "/parent/objectId"),
                @SearchableField(name = "applicationName", path = "/parent/name"),
                @SearchableField(name = "description", path = "/description"),
                @SearchableField(name = "platform", path = "/platform"),
                @SearchableField(name = "active", path = "/active")
        }
)
@Indexes(@Index(value = "parent_application, platform", unique = true))
public abstract class AbstractMongoApplicationProfile {

    @Id
    private String objectId;

    @Reference("parent")
    private MongoApplication parent;

    @Property("platform")
    private Platform platform;

    @Property("active")
    private boolean active;

    @Property("class_heierarchy")
    private Set<String> classHierarchy;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
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

    /**
     * This is here because Morphia currently doesn't support polymorphic queries.
     */
    @PrePersist
    public void storeTypeHierarchy() {

        classHierarchy = new HashSet<>();

        Class<?> cls = getClass();

        do {
            classHierarchy.add(cls.getName());
            cls = cls.getSuperclass();
        } while (cls != null);

    }

}
