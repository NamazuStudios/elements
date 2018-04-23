package com.namazustudios.socialengine.dao.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Property;

import java.util.Base64;

import static java.lang.System.arraycopy;

public class MongoScoreId {

    private static final int OBJECT_ID_LENGTH = 12;

    @Property
    private ObjectId profileId;

    @Property
    private ObjectId leaderboardId;

    MongoScoreId() {}

    public MongoScoreId(final String hexString) {

        final byte[] bytes = Base64.getDecoder().decode(hexString);

        if (bytes.length != OBJECT_ID_LENGTH * 2) {
            throw new IllegalArgumentException("Expecting length of " + OBJECT_ID_LENGTH * 2 + " bytes.");
        }

        final byte[] profileIdBytes = new byte[OBJECT_ID_LENGTH];
        final byte[] leaderboardIdBytes = new byte[OBJECT_ID_LENGTH];
        arraycopy(bytes, 0, profileIdBytes, 0, OBJECT_ID_LENGTH);
        arraycopy(bytes, OBJECT_ID_LENGTH, leaderboardIdBytes, 0, OBJECT_ID_LENGTH);

        profileId = new ObjectId(profileIdBytes);
        leaderboardId = new ObjectId(leaderboardIdBytes);

    }

    public MongoScoreId(final MongoProfile mongoProfile, final MongoLeaderboard mongoLeaderboard) {
        this(mongoProfile.getObjectId(), mongoProfile.getObjectId());
    }

    public MongoScoreId(final ObjectId profileId, final ObjectId leaderboardId) {
        this.profileId = profileId;
        this.leaderboardId = leaderboardId;
    }

    public ObjectId getProfileId() {
        return profileId;
    }

    public ObjectId getLeaderboardId() {
        return leaderboardId;
    }

    public byte[] toByteArray() {
        final byte[] profileIdBytes = profileId.toByteArray();
        final byte[] leaderboardIdBytes = leaderboardId.toByteArray();
        final byte[] bytes = new byte[profileIdBytes.length + leaderboardIdBytes.length];
        arraycopy(profileIdBytes, 0, bytes, 0, profileIdBytes.length);
        arraycopy(leaderboardIdBytes, 0, bytes, profileIdBytes.length, leaderboardIdBytes.length);
        return bytes;
    }

    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

}
