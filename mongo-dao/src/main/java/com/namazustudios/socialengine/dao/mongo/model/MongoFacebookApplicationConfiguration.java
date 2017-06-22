package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 6/15/17.
 */
@SearchableDocument
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoFacebookApplicationConfiguration extends MongoApplicationConfiguration {

    @Property("applicationSecret")
    private String applicationSecret;

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }
}
