package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Entity(value = "application_configuration", useDiscriminator = false)
public class MongoPSNApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String clientSecret;

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

}
