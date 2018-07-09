package com.namazustudios.socialengine.dao.mongo.model.application;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import org.mongodb.morphia.annotations.Entity;

/**
 * Created by patricktwohig on 5/31/17.
 */
@SearchableDocument
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoIosApplicationConfiguration extends MongoApplicationConfiguration {
    // TODO This will likely be populated with more information.
    // The unique app id (eg com.mycompany.myapp) is stored as the unique ID in the parent
    // class.  This will likely include additional information such as the server side
    // certificates and what not to enable push notifications or verify purchases.
}
