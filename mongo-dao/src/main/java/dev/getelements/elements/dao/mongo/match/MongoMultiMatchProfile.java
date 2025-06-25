package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.sql.Timestamp;

import static dev.getelements.elements.dao.mongo.match.MongoMultiMatch.EXPIRY_SECONDS;

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

    @Indexed(options = @IndexOptions(expireAfterSeconds = EXPIRY_SECONDS))
    private Timestamp expiry;

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

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
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
