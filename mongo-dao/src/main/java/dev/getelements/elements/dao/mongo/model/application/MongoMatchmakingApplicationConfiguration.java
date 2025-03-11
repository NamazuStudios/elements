package dev.getelements.elements.dao.mongo.model.application;

import dev.getelements.elements.dao.mongo.model.MongoCallbackDefinition;
import dev.getelements.elements.sdk.model.match.MatchingAlgorithm;
import dev.morphia.annotations.Entity;

@Entity(value = "application_configuration")
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
