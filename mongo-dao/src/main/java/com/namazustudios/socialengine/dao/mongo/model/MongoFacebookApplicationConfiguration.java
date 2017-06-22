package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 6/15/17.
 */
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
