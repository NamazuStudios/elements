package com.namazustudios.promotion.dao.mongo.model;

import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity("short_link")
public class MongoShortLink {

    @Id
    private Key<String> objectId;

    @Property
    private String destinationUrl;

    public Key<String> getObjectId() {
        return objectId;
    }

    public void setObjectId(Key<String> objectId) {
        this.objectId = objectId;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

}
