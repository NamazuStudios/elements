package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

public class MongoMultiMatchProfile {

    @Id
    private ObjectId id;

    @Indexed
    @Reference
    private MultiMatch match;

    @Indexed
    @Reference
    private MongoProfile profile;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public MultiMatch getMatch() {
        return match;
    }

    public void setMatch(MultiMatch match) {
        this.match = match;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }
}
