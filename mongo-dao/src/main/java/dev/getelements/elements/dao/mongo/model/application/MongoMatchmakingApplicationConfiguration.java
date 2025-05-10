package dev.getelements.elements.dao.mongo.model.application;

import dev.getelements.elements.dao.mongo.model.MongoCallbackDefinition;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@Entity(value = "application_configuration")
public class MongoMatchmakingApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private MongoCallbackDefinition success;

    @Property
    private MongoElementServiceReference matchmaker;

    public MongoCallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(MongoCallbackDefinition success) {
        this.success = success;
    }

    public MongoElementServiceReference getMatchmaker() {
        return matchmaker;
    }

    public void setMatchmaker(MongoElementServiceReference matchmaker) {
        this.matchmaker = matchmaker;
    }

}
