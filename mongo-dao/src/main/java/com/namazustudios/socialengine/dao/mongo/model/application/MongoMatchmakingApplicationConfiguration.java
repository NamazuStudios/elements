package com.namazustudios.socialengine.dao.mongo.model.application;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.dao.mongo.model.MongoCallbackDefinition;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import dev.morphia.annotations.Entity;

/**
 * Maps to {@link com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration}.
 */
@SearchableDocument
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoMatchmakingApplicationConfiguration extends MongoApplicationConfiguration {

    private MatchingAlgorithm algorithm;

    private MongoCallbackDefinition success;

    public MatchingAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(MatchingAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public MongoCallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(MongoCallbackDefinition success) {
        this.success = success;
    }

}
