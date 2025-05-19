package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Entity(value = "application_configuration")
public class MongoPSNApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String npIdentifier;

    @Property
    private String clientSecret;

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
