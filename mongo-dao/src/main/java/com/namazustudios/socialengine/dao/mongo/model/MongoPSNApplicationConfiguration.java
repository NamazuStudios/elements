package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.mongodb.morphia.annotations.*;

/**
 * Created by patricktwohig on 7/10/15.
 */
@SearchableDocument
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
