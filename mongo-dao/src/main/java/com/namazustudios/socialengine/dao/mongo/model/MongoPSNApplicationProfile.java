package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Entity(value = "psn_application_profile", noClassnameStored = true)
public class MongoPSNApplicationProfile {

    @Id
    private String id;

    @Property("np_id")
    @Indexed(unique = true)
    private String npIdentifier;

    @Property("client_secret")
    private String clientSecret;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNpIdentifier() {
        return npIdentifier;
    }

    public void setNpIdentifier(String npIdentifier) {
        this.npIdentifier = npIdentifier;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

}
