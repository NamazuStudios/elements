package dev.getelements.elements.dao.mongo.model.application;

import dev.getelements.elements.dao.mongo.model.MongoCallbackDefinition;
import dev.morphia.annotations.Entity;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity(value = "application_configuration")
public class MongoMatchmakingApplicationConfiguration extends MongoApplicationConfiguration {

    private String matchmakerName;

    private String matchmakerElement;

    private MongoCallbackDefinition success;

    public String getMatchmakerName() {
        return matchmakerName;
    }

    public void setMatchmakerName(String matchmakerName) {
        this.matchmakerName = matchmakerName;
    }

    public String getMatchmakerElement() {
        return matchmakerElement;
    }

    public void setMatchmakerElement(String matchmakerElement) {
        this.matchmakerElement = matchmakerElement;
    }

    public MongoCallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(MongoCallbackDefinition success) {
        this.success = success;
    }

}
