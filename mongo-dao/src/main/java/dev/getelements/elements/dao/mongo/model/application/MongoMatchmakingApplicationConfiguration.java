package dev.getelements.elements.dao.mongo.model.application;

import dev.getelements.elements.dao.mongo.model.MongoCallbackDefinition;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

import java.util.Map;

@Entity(value = "application_configuration")
public class MongoMatchmakingApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private MongoCallbackDefinition success;

    @Property
    private MongoElementServiceReference matchmaker;

    @Property
    private int maxProfiles;

    @Property
    private Map<String, Object> metadata;

    @Property
    private MongoMetadataSpec metadataSpec;

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

    public int getMaxProfiles() {
        return maxProfiles;
    }

    public void setMaxProfiles(int maxProfiles) {
        this.maxProfiles = maxProfiles;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public MongoMetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    public void setMetadataSpec(MongoMetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
    }

}
