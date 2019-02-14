package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.NotFoundException;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.lang.System.arraycopy;

@Embedded
public class MongoRewardIssuanceId {

    private static final int USER_ID_INDEX = 0;

    private static final int REWARD_ID_INDEX = 1;

    private static final int CONTEXT_INDEX = 2;

    private static final int OBJECT_ID_LENGTH = 12;

    private static final Charset CONTEXT_CHARSET = StandardCharsets.UTF_8;

    @Property
    private ObjectId userId;

    @Property
    private ObjectId rewardId;

    @Property
    private String context;

    public MongoRewardIssuanceId() {}

    public MongoRewardIssuanceId(final String hexString) {
        if (hexString.getBytes().length < 2) {
            throw new IllegalArgumentException("Provided RewardIssuance is too short.");
        }

        final byte [] bytes = Base64.getDecoder().decode(hexString);
        if (bytes.length <= (OBJECT_ID_LENGTH * 2)) {
            throw new IllegalArgumentException("Provided RewardIssuance id is too short.");
        }

        final byte[] objectIdBytes = new byte[OBJECT_ID_LENGTH];

        final int contextByteLength = bytes.length - OBJECT_ID_LENGTH * CONTEXT_INDEX;

        arraycopy(bytes, OBJECT_ID_LENGTH * USER_ID_INDEX, objectIdBytes, 0, objectIdBytes.length);
        userId = new ObjectId(objectIdBytes);

        arraycopy(bytes, OBJECT_ID_LENGTH * REWARD_ID_INDEX, objectIdBytes, 0, objectIdBytes.length);
        rewardId = new ObjectId(objectIdBytes);

        final byte[] contextBytes = new byte[contextByteLength];

        arraycopy(bytes, OBJECT_ID_LENGTH * CONTEXT_INDEX,  contextBytes,
                0, contextByteLength);

        context = new String(contextBytes, CONTEXT_CHARSET);
    }

    public MongoRewardIssuanceId(final MongoUser mongoUser,
                                 final MongoReward mongoReward,
                                 final String context) {
        this(mongoUser.getObjectId(), mongoReward.getObjectId(), context);
    }

    public MongoRewardIssuanceId(final ObjectId userId, final ObjectId rewardId, final String context) {
        this.userId = userId;
        this.rewardId = rewardId;
        if (userId == null || rewardId == null || context == null) {
            throw new IllegalArgumentException("Must specify both ids as well as context.");
        }

        if (context.length() == 0) {
            throw new IllegalArgumentException("context must not be empty.");
        }

        this.context = context;
    }

    public byte[] toByteArray() {

        final byte[] userIdBytes = userId.toByteArray();
        final byte[] rewardIdBytes = rewardId.toByteArray();
        final byte[] contextBytes = context.getBytes(CONTEXT_CHARSET);
        final byte[] bytes = new byte[OBJECT_ID_LENGTH * 2 + contextBytes.length];

        arraycopy(userIdBytes, 0, bytes, OBJECT_ID_LENGTH * USER_ID_INDEX, userIdBytes.length);
        arraycopy(rewardIdBytes, 0, bytes, OBJECT_ID_LENGTH * REWARD_ID_INDEX, rewardIdBytes.length);
        arraycopy(contextBytes, 0, bytes, OBJECT_ID_LENGTH * CONTEXT_INDEX, contextBytes.length);

        return bytes;

    }

    public String toHexString() {
        final byte[] bytes = toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public ObjectId getRewardId() {
        return rewardId;
    }

    public void setRewardId(ObjectId rewardId) {
        this.rewardId = rewardId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoRewardIssuanceId)) return false;
        MongoRewardIssuanceId that = (MongoRewardIssuanceId) object;
        return Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getRewardId(), that.getRewardId()) &&
                Objects.equals(getContext(), that.getContext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getRewardId(), getContext());
    }

    @Override
    public String toString() {
        return "MongoRewardIssuanceId{" +
                "userId=" + userId +
                ", rewardId=" + rewardId +
                ", context=" + context +
                '}';
    }

    public static MongoRewardIssuanceId parseOrThrowNotFoundException(final String id) {
        try {
            return new MongoRewardIssuanceId(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ex);
        }
    }
}
