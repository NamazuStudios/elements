package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;

import java.util.Map;

/**
 * Created by patricktwohig on 5/31/17.
 */
@Entity(value = "application_configuration")
public class MongoGooglePlayApplicationConfiguration extends MongoApplicationConfiguration {
    // TODO This will likely be populated with more information.
    // The unique app id (eg com.mycompany.myapp) is stored as the unique ID in the parent
    // class.  This will likely include additional information such as the server side
    // certificates and what not to enable push notifications or verify purchases.

    private Map<String, Object> jsonKey;

    public Map<String, Object> getJsonKey() {
        return jsonKey;
    }

    public void setJsonKey(Map<String, Object> jsonKey) {
        this.jsonKey = jsonKey;
    }


}
