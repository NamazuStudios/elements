package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.util.Hex;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.util.Base64;
import java.util.Objects;

import static java.lang.System.arraycopy;

@Embedded
public class MongoProgressId {

    private static final int PROFILE_ID_INDEX = 0;

    private static final int MISSION_ID_INDEX = 1;

    private static final int OBJECT_ID_LENGTH = 12;

    @Indexed
    private ObjectId profileId;

    @Indexed
    @Property
    private ObjectId missionId;

    public MongoProgressId() {}

    public MongoProgressId(final String hexString) {

        final byte [] bytes = Hex.decode(hexString);
        if (bytes.length != (OBJECT_ID_LENGTH * 2)) throw new IllegalArgumentException();

        final byte[] objectIdBytes = new byte[OBJECT_ID_LENGTH];

        arraycopy(bytes, OBJECT_ID_LENGTH * PROFILE_ID_INDEX, objectIdBytes, 0, objectIdBytes.length);
        profileId = new ObjectId(objectIdBytes);

        arraycopy(bytes, OBJECT_ID_LENGTH * MISSION_ID_INDEX, objectIdBytes, 0, objectIdBytes.length);
        missionId = new ObjectId(objectIdBytes);

    }

    public MongoProgressId(final MongoProfile mongoProfile,
                           final MongoMission mongoMission) {
        this(mongoProfile.getObjectId(), mongoMission.getObjectId());
    }

    public MongoProgressId(final ObjectId profileId, final ObjectId missionId) {
        this.profileId = profileId;
        this.missionId = missionId;
        if (profileId == null || missionId == null) throw new IllegalArgumentException("Must specify both ids.");
    }
    public byte[] toByteArray() {

        final byte[] profileIdBytes = profileId.toByteArray();
        final byte[] missionIdBytes = missionId.toByteArray();
        final byte[] bytes = new byte[OBJECT_ID_LENGTH * 2];

        arraycopy(profileIdBytes, 0, bytes, OBJECT_ID_LENGTH * PROFILE_ID_INDEX, profileIdBytes.length);
        arraycopy(missionIdBytes, 0, bytes, OBJECT_ID_LENGTH * MISSION_ID_INDEX, missionIdBytes.length);

        return bytes;

    }

    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Hex.encode(bytes);
    }

    public ObjectId getProfileId() {
        return profileId;
    }

    public void setProfileId(ObjectId profileId) {
        this.profileId = profileId;
    }

    public ObjectId getMissionId() {
        return missionId;
    }

    public void setMissionId(ObjectId missionId) {
        this.missionId = missionId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoProgressId)) return false;
        MongoProgressId that = (MongoProgressId) object;
        return Objects.equals(getProfileId(), that.getProfileId()) &&
               Objects.equals(getMissionId(), that.getMissionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProfileId(), getMissionId());
    }

    @Override
    public String toString() {
        return "MongoProgressId{" +
                "profileId=" + profileId +
                ", missionId=" + missionId +
                '}';
    }

    public static MongoProgressId parseOrThrowNotFoundException(final String inventoryItemId) {
        try {
            return new MongoProgressId(inventoryItemId);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ex);
        }
    }

}
