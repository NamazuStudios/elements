package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.application.MongoMatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.match.MultiMatchStatus;
import dev.morphia.annotations.*;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Entity("multi_match")
public class MongoMultiMatch {

    public static final int EXPIRY_SECONDS = 3600;

    @Id
    private ObjectId id;

    @NotNull
    @Property
    private MultiMatchStatus status;

    @Reference
    private MongoApplication application;

    @Reference
    private MongoMatchmakingApplicationConfiguration configuration;

    @Property
    private Map<String, Object> metadata;

    @Indexed
    @Property
    private Timestamp created;

    @Property
    @Indexed(options = @IndexOptions(expireAfterSeconds = EXPIRY_SECONDS))
    private Timestamp expiry;

    @Indexed
    @Reference
    private List<MongoProfile> profiles;

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

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    public List<MongoProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<MongoProfile> profiles) {
        this.profiles = profiles;
    }

}
