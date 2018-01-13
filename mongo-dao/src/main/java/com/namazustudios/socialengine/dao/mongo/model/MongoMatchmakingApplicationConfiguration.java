package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.mongodb.morphia.annotations.Entity;

/**
 * Maps to {@link com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration}.
 */
@SearchableDocument
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoMatchmakingApplicationConfiguration extends MongoApplicationConfiguration {

    private MongoCallbackDefinition success;

    public MongoCallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(MongoCallbackDefinition success) {
        this.success = success;
    }

}
