package com.namazustudios.socialengine.dao.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Property;

import java.nio.ByteBuffer;
import java.util.Base64;

import static java.lang.System.arraycopy;

public class MongoScoreId {

    private static final int OBJECT_ID_LENGTH = 12;
    private static final int BYTE_LENGTH = OBJECT_ID_LENGTH * 2 + Long.BYTES;

    @Property
    private ObjectId profileId;

    @Property
    private ObjectId leaderboardId;

    @Property
    /**
     * By convention, if the leaderboard is global instead of epochal, we record the leaderboardEpoch as 0. This is to
     * maintain consistency across all mongo scores, i.e. all mongo score ids will always be composed of three non-null
     * elements.
     */
    private Long leaderboardEpoch;

    MongoScoreId() {}

    public MongoScoreId(final String hexString) {

        final byte[] bytes = Base64.getDecoder().decode(hexString);

        if (bytes.length != BYTE_LENGTH) {
            throw new IllegalArgumentException("Expecting length of " + BYTE_LENGTH + " bytes.");
        }

        final byte[] profileIdBytes = new byte[OBJECT_ID_LENGTH];
        final byte[] leaderboardIdBytes = new byte[OBJECT_ID_LENGTH];
        final byte[] leaderboardEpochBytes = new byte[Long.BYTES];
        arraycopy(bytes, 0, profileIdBytes, 0, OBJECT_ID_LENGTH);
        arraycopy(bytes, OBJECT_ID_LENGTH, leaderboardIdBytes, 0, OBJECT_ID_LENGTH);
        arraycopy(bytes, OBJECT_ID_LENGTH * 2, leaderboardEpochBytes, 0, Long.BYTES);

        profileId = new ObjectId(profileIdBytes);
        leaderboardId = new ObjectId(leaderboardIdBytes);

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        leaderboardEpoch = buffer.getLong();
    }

    public MongoScoreId(final MongoProfile mongoProfile, final MongoLeaderboard mongoLeaderboard) {
        this(mongoProfile.getObjectId(), mongoLeaderboard.getObjectId(), 0L);
    }

    public MongoScoreId(final ObjectId profileId, final ObjectId leaderboardId) {
        this(profileId, leaderboardId, 0L);
    }

    public MongoScoreId(final MongoProfile mongoProfile, final MongoLeaderboard mongoLeaderboard, final Long leaderboardEpoch) {
        this(mongoProfile.getObjectId(), mongoLeaderboard.getObjectId(), leaderboardEpoch);
    }

    public MongoScoreId(final ObjectId profileId, final ObjectId leaderboardId, final Long leaderboardEpoch) {
        this.profileId = profileId;
        this.leaderboardId = leaderboardId;
        this.leaderboardEpoch = leaderboardEpoch;
    }

    public ObjectId getProfileId() {
        return profileId;
    }

    public ObjectId getLeaderboardId() {
        return leaderboardId;
    }

    public Long getLeaderboardEpoch() {
        return leaderboardEpoch;
    }

    public byte[] toByteArray() {
        final byte[] profileIdBytes = profileId.toByteArray();
        final byte[] leaderboardIdBytes = leaderboardId.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(leaderboardEpoch);
        final byte[] leaderboardEpochBytes = buffer.array();

        final byte[] bytes = new byte[profileIdBytes.length + leaderboardIdBytes.length + leaderboardEpochBytes.length];

        arraycopy(profileIdBytes, 0, bytes, 0, profileIdBytes.length);
        arraycopy(leaderboardIdBytes, 0, bytes, profileIdBytes.length, leaderboardIdBytes.length);
        arraycopy(leaderboardEpochBytes, 0, bytes, profileIdBytes.length + leaderboardIdBytes.length, leaderboardEpochBytes.length);
        return bytes;
    }

    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

}
