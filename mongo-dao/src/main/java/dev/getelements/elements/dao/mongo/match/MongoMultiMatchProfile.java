package dev.getelements.elements.dao.mongo.match;

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
    private Profile profile;

    @Indexed
    @Reference
    private MultiMatch match;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public MultiMatch getMatch() {
        return match;
    }

    public void setMatch(MultiMatch match) {
        this.match = match;
    }
}
