package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

/**
 * Created by patricktwohig on 6/15/17.
 */
@Entity(value = "application_configuration")
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
