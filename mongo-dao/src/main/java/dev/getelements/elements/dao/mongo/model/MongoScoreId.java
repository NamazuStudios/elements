package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.dao.mongo.HexableId;
import dev.getelements.elements.rt.util.Hex;
import dev.morphia.annotations.Entity;
import org.bson.types.ObjectId;
import dev.morphia.annotations.Property;

import java.nio.ByteBuffer;
import java.util.Base64;

import static java.lang.System.arraycopy;

@Entity
public class MongoScoreId implements HexableId {

    /**
     * By convention, if the leaderboard is global instead of epochal, we record the leaderboardEpoch as 0L. This is to
     * maintain consistency across all mongo scores, i.e. all mongo score ids will always be composed of three non-null
     * elements.
     */
    public static final long ALL_TIME_LEADERBOARD_EPOCH = 0L;

    private static final int OBJECT_ID_LENGTH = 12;
    private static final int BYTE_LENGTH = OBJECT_ID_LENGTH * 2 + Long.BYTES;

    @Property
    private ObjectId profileId;

    @Property
    private ObjectId leaderboardId;

    @Property
    private long leaderboardEpoch;

    public MongoScoreId() {}

    public MongoScoreId(final String hexString) {

        final byte[] bytes = Hex.decode(hexString);

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
        this(mongoProfile.getObjectId(), mongoLeaderboard.getObjectId(), ALL_TIME_LEADERBOARD_EPOCH);
    }

    public MongoScoreId(final ObjectId profileId, final ObjectId leaderboardId) {
        this(profileId, leaderboardId, ALL_TIME_LEADERBOARD_EPOCH);
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

    @Override
    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Hex.encode(bytes);
    }

}
