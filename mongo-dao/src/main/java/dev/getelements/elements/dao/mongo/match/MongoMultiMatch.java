package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.application.MongoMatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.match.MultiMatchStatus;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

import java.util.Map;

public class MongoMultiMatch {

    @Id
    private ObjectId id;

    @Property
    private MultiMatchStatus status;

    @Reference
    private MongoApplication application;

    @Reference
    private MongoMatchmakingApplicationConfiguration configuration;

    @Reference
    private Map<String, Object> metadata;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public MultiMatchStatus getStatus() {
        return status;
    }

    public void setStatus(MultiMatchStatus status) {
        this.status = status;
    }

    public MongoApplication getApplication() {
        return application;
    }

    public void setApplication(MongoApplication application) {
        this.application = application;
    }

    public MongoMatchmakingApplicationConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(MongoMatchmakingApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

}
