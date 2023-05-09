package dev.getelements.elements.dao.mongo.model.application;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import dev.getelements.elements.dao.mongo.model.MongoCallbackDefinition;
import dev.getelements.elements.model.match.MatchingAlgorithm;
import dev.morphia.annotations.Entity;

/**
 * Maps to {@link dev.getelements.elements.model.application.MatchmakingApplicationConfiguration}.
 */
@SearchableDocument
@Entity(value = "application_configuration", useDiscriminator = false)
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
