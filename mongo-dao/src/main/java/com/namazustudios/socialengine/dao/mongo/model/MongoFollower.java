package com.namazustudios.socialengine.dao.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "follower", noClassnameStored = true)
public class MongoFollower {

    @Id
    private ObjectId objectId;

    @Property
    private String profileId;

    @Property
    private String followedId;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getProfileId() {return profileId; }

    public void setProfileId(String profileId) {this.profileId = profileId; }

    public String getFollowedId() {return followedId; }

    public void setFollowedId(String followedId) {this.followedId = followedId; }

}
