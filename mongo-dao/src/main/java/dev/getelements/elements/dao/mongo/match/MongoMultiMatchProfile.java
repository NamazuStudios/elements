package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

@Entity("multi_match_profile")
public class MongoMultiMatchProfile {

    @Id
    private ID id;

    @Indexed
    @Reference
    private MongoMultiMatch match;

    @Indexed
    @Reference
    private MongoProfile profile;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public MongoMultiMatch getMatch() {
        return match;
    }

    public void setMatch(MongoMultiMatch match) {
        this.match = match;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public static class ID {

        private ObjectId matchId;

        private ObjectId profileId;

        public ID() {}

        public ID(MongoMultiMatch match, MongoProfile profile) {
            matchId = match.getId();
            profileId = profile.getObjectId();
        }

        public ObjectId getMatchId() {
            return matchId;
        }

        public void setMatchId(ObjectId matchId) {
            this.matchId = matchId;
        }

        public ObjectId getProfileId() {
            return profileId;
        }

        public void setProfileId(ObjectId profileId) {
            this.profileId = profileId;
        }

    }

}
