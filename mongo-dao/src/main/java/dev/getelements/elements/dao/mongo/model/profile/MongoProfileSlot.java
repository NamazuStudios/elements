package dev.getelements.elements.dao.mongo.model.profile;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

/**
 * Records that a slot has been allocated for a given user and application. Slot {@code 0} is defined as the
 * user's primary profile for that application. This document is never exposed via the SDK model -- it exists
 * purely to make slot assignment and the {@code maxProfiles} check race-safe under concurrent profile creation.
 */
@Indexes({
        @Index(fields = { @Field("_id.userId"), @Field("_id.applicationId") })
})
@Entity(value = "profile_slot", useDiscriminator = false)
public class MongoProfileSlot {

    @Id
    private MongoProfileSlotId objectId;

    @Property
    private ObjectId profileId;

    public MongoProfileSlotId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoProfileSlotId objectId) {
        this.objectId = objectId;
    }

    public ObjectId getProfileId() {
        return profileId;
    }

    public void setProfileId(ObjectId profileId) {
        this.profileId = profileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoProfileSlot)) return false;
        MongoProfileSlot that = (MongoProfileSlot) o;
        return getObjectId() != null ? getObjectId().equals(that.getObjectId()) : that.getObjectId() == null;
    }

    @Override
    public int hashCode() {
        return getObjectId() != null ? getObjectId().hashCode() : 0;
    }

}
