package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.*;

@Indexes({
    @Index(fields = @Field("_id.profileId")),
    @Index(fields = @Field("_id.followedId"))
})
@Entity("follower")
public class MongoFollower {

    @Id
    private MongoFollowerId objectId;

    @Reference
    private MongoProfile followedProfile;

    public MongoFollowerId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoFollowerId objectId) {
        this.objectId = objectId;
    }

    public MongoProfile getFollowedProfile() {return followedProfile; }

    public void setFollowedProfile(MongoProfile followedProfile) {this.followedProfile = followedProfile; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoFollower)) return false;

        MongoFollower mongoFollower = (MongoFollower) o;

        if (getObjectId() != null ? !getObjectId().getProfileId().equals(mongoFollower.getObjectId().getProfileId()) : mongoFollower.getObjectId().getProfileId() != null) return false;
        if (getObjectId() != null ? !getObjectId().getFollowedId().equals(mongoFollower.getObjectId().getFollowedId()) : mongoFollower.getObjectId().getFollowedId() != null) return false;
        return getFollowedProfile() != null ? getFollowedProfile().equals(mongoFollower.getFollowedProfile()) : mongoFollower.getFollowedProfile() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getObjectId().getProfileId() != null ? getObjectId().getProfileId().hashCode() : 0);
        result = 31 * result + (getObjectId().getFollowedId() != null ? getObjectId().getFollowedId().hashCode() : 0);
        result = 31 * result + (getFollowedProfile() != null ? getFollowedProfile().hashCode() : 0);
        return result;
    }

}
