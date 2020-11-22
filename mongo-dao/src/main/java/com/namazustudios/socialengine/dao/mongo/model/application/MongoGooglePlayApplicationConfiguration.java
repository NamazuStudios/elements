package com.namazustudios.socialengine.dao.mongo.model.application;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import dev.morphia.annotations.Entity;

import java.util.Map;

/**
 * Created by patricktwohig on 5/31/17.
 */
@SearchableDocument
@Entity(value = "application_configuration", useDiscriminator = false)
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
