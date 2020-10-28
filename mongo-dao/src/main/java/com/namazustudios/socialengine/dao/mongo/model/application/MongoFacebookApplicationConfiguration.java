package com.namazustudios.socialengine.dao.mongo.model.application;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

/**
 * Created by patricktwohig on 6/15/17.
 */
@SearchableDocument
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoFacebookApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String applicationSecret;

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }
}
