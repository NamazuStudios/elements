package dev.getelements.elements.dao.mongo.model;

import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.annotations.*;

import java.util.Objects;

import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.exists;
import static dev.morphia.query.updates.UpdateOperators.set;

@Indexes({
    @Index(fields = @Field("_id.profileId")),
    @Index(fields = @Field("_id.followedId"))
})
@Entity("follower")
public class MongoFollower {

    @Id
    private MongoFollowerId objectId;

    @Reference
    private MongoProfile profile;

    @Reference
    private MongoProfile followedProfile;

    public MongoFollowerId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoFollowerId objectId) {
        this.objectId = objectId;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public MongoProfile getFollowedProfile() {return followedProfile; }

    public void setFollowedProfile(MongoProfile followedProfile) {this.followedProfile = followedProfile; }

    @PostLoad
    public void postLoad(final Datastore datastore) {
        if (getProfile() == null) {

            // This is here to ensure old data sets will properly fetch the profile.

            profile = datastore.find(MongoProfile.class).filter(
                    eq("_id", getObjectId().getProfileId())
            ).first();

            datastore
                    .find(MongoFollower.class)
                    .filter(eq("_id", getObjectId()), exists("profile").not())
                    .modify(new ModifyOptions(), set("profile", profile));

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoFollower that = (MongoFollower) o;
        return Objects.equals(getObjectId(), that.getObjectId()) && Objects.equals(getProfile(), that.getProfile()) && Objects.equals(getFollowedProfile(), that.getFollowedProfile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getProfile(), getFollowedProfile());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoFollower{");
        sb.append("objectId=").append(objectId);
        sb.append(", profile=").append(profile);
        sb.append(", followedProfile=").append(followedProfile);
        sb.append('}');
        return sb.toString();
    }

}
