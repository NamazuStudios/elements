package com.namazustudios.socialengine.dao.mongo.model;

import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

/**
 * Created by patricktwohig on 6/28/17.
 */
@Entity(value = "score", noClassnameStored = true)
@Indexes({
    @Index(fields = @Field("profile")),
    @Index(fields = @Field("leaderboard")),
    @Index(fields = @Field(value = "pointValue", type = IndexType.DESC))
})
public class MongoScore {

    @Id
    private MongoScoreId objectId;

    @Property
    private double pointValue;

    @Property
    private long creationTimestamp;

    @Reference
    private MongoProfile profile;

    @Reference
    private MongoLeaderboard leaderboard;

    public MongoScoreId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoScoreId objectId) {
        this.objectId = objectId;
    }

    public double getPointValue() {
        return pointValue;
    }

    public void setPointValue(double pointValue) {
        this.pointValue = pointValue;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public MongoLeaderboard getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(MongoLeaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

}
