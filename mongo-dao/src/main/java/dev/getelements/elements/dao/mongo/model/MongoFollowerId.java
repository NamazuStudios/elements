package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.Embedded;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Embedded
public class MongoFollowerId implements Serializable {

    private ObjectId profileId;

    private ObjectId followedId;

    public MongoFollowerId() {}

    public MongoFollowerId(String profileId, String followedId){
        this.profileId = new ObjectId(profileId);
        this.followedId = new ObjectId(followedId);
    }

    public MongoFollowerId(ObjectId profileId, ObjectId followedId) {
        this.profileId = profileId;
        this.followedId = followedId;
    }

    public ObjectId getProfileId(){ return profileId; }

    public void setProfileId(ObjectId profileId){ this.profileId = profileId; }

    public ObjectId getFollowedId(){ return followedId; }

    public void setFollowedId(ObjectId followedId){ this.followedId = followedId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoFollowerId)) return false;

        MongoFollowerId mongoFollowerId = (MongoFollowerId) o;

        if (getProfileId() != null ? !getProfileId().equals(mongoFollowerId.getProfileId()) : mongoFollowerId.getProfileId() != null) return false;
        return getFollowedId() != null ? getFollowedId().equals(mongoFollowerId.getFollowedId()) : mongoFollowerId.getFollowedId() == null;
    }

    @Override
    public int hashCode() {
        int result = getProfileId() != null ? getProfileId().hashCode() : 0;
        result = 31 * result + (getFollowedId() != null ? getFollowedId().hashCode() : 0);
        return result;
    }

}
