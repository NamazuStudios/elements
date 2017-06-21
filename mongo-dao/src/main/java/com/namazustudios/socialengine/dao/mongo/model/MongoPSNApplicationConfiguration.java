package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.*;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoPSNApplicationConfiguration extends MongoApplicationConfiguration {

    @Property("client_secret")
    private String clientSecret;

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

}
