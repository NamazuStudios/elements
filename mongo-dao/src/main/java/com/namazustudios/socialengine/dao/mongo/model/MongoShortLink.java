package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity(value = "short_link", noClassnameStored = true)
@SearchableIdentity(@SearchableField(name = "id", path = "/objectId", type = String.class))
@SearchableDocument(fields = @SearchableField(name = "destinationUrl", path = "/destinationUrl"))
public class MongoShortLink {

    @Id
    private String objectId;

    @Property("destination_url")
    private String destinationUrl;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

}
