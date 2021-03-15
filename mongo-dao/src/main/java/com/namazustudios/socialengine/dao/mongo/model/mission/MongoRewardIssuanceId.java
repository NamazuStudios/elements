package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import static java.lang.System.arraycopy;

@Embedded
public class MongoRewardIssuanceId {

    private static final int USER_ID_LENGTH = 12;
    private static final int USER_ID_START_POSITION = 0;

    private static final int ITEM_ID_LENGTH = 12;
    private static final int ITEM_ID_START_POSITION = USER_ID_START_POSITION + USER_ID_LENGTH;

    private static final int ITEM_QUANTITY_LENGTH = 4;
    private static final int ITEM_QUANTITY_START_POSITION = ITEM_ID_START_POSITION + ITEM_ID_LENGTH;

    private static final int CONTEXT_BYTE_START_POSITION = ITEM_QUANTITY_START_POSITION + ITEM_QUANTITY_LENGTH;

    private static final Charset CONTEXT_CHARSET = StandardCharsets.UTF_8;

    @Property
    private ObjectId userId;

    @Property
    private ObjectId itemId;

    @Property
    private int itemQuantity;

    @Property
    private String context;

    public MongoRewardIssuanceId() {}

    public MongoRewardIssuanceId(final String hexString) {
        if (hexString.getBytes().length < 2) {
            throw new IllegalArgumentException("Provided RewardIssuance id is too short.");
        }

        final byte [] bytes = Base64.getDecoder().decode(hexString);

        if (bytes.length <= CONTEXT_BYTE_START_POSITION) {
            throw new IllegalArgumentException("Provided RewardIssuance id is too short.");
        }

        final byte[] userIdBytes = new byte[USER_ID_LENGTH];
        arraycopy(bytes, USER_ID_START_POSITION, userIdBytes, 0, USER_ID_LENGTH);
        userId = new ObjectId(userIdBytes);

        final byte[] itemIdBytes = new byte[ITEM_ID_LENGTH];
        arraycopy(bytes, ITEM_ID_START_POSITION, itemIdBytes, 0, ITEM_ID_LENGTH);
        itemId = new ObjectId(itemIdBytes);

        final byte[] itemQuantityBytes = new byte[ITEM_QUANTITY_LENGTH];
        arraycopy(bytes, ITEM_QUANTITY_START_POSITION, itemQuantityBytes, 0, ITEM_QUANTITY_LENGTH);
        itemQuantity = ByteBuffer.wrap(itemQuantityBytes).getInt();

        final int contextByteLength = bytes.length - CONTEXT_BYTE_START_POSITION;

        final byte[] contextBytes = new byte[contextByteLength];
        arraycopy(bytes, CONTEXT_BYTE_START_POSITION, contextBytes, 0, contextByteLength);
        context = new String(contextBytes, CONTEXT_CHARSET);
    }

    public MongoRewardIssuanceId(final MongoUser mongoUser,
                                 final MongoItem mongoItem,
                                 final int itemQuantity,
                                 final String context) {
        this(mongoUser.getObjectId(), mongoItem.getObjectId(), itemQuantity, context);
    }

    public MongoRewardIssuanceId(
            final ObjectId userId,
            final ObjectId itemId,
            final int itemQuantity,
            final String context
    ) {
        this.userId = userId;
        this.itemId = itemId;
        if (userId == null || itemId == null || context == null) {
            throw new IllegalArgumentException("Must specify both ids as well as context.");
        }

        if (context.length() == 0) {
            throw new IllegalArgumentException("Context must not be empty.");
        }

        if (itemQuantity < 0) {
            throw new IllegalArgumentException("Item Quantity must be non-negative.");
        }

        this.context = context;
    }

    public byte[] toByteArray() {

        final byte[] userIdBytes = getUserId().toByteArray();
        final byte[] itemIdBytes = getItemId().toByteArray();
        final byte[] itemQuantityBytes = ByteBuffer.allocate(4).putInt(getItemQuantity()).array();
        final byte[] contextBytes = getContext().getBytes(CONTEXT_CHARSET);

        final byte[] destinationBytes = new byte[CONTEXT_BYTE_START_POSITION + contextBytes.length];

        arraycopy(userIdBytes, 0, destinationBytes, USER_ID_START_POSITION, USER_ID_LENGTH);
        arraycopy(itemIdBytes, 0, destinationBytes, ITEM_ID_START_POSITION, ITEM_ID_LENGTH);
        arraycopy(itemQuantityBytes, 0, destinationBytes, ITEM_QUANTITY_START_POSITION, ITEM_QUANTITY_LENGTH);
        arraycopy(contextBytes, 0, destinationBytes, CONTEXT_BYTE_START_POSITION, contextBytes.length);

        return destinationBytes;

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

    public ObjectId getItemId() {
        return itemId;
    }

    public void setItemId(ObjectId itemId) {
        this.itemId = itemId;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoRewardIssuanceId that = (MongoRewardIssuanceId) o;
        return getItemQuantity() == that.getItemQuantity() &&
                Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getItemId(), that.getItemId()) &&
                Objects.equals(getContext(), that.getContext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getItemId(), getItemQuantity(), getContext());
    }

    @Override
    public String toString() {
        return "MongoRewardIssuanceId{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", itemQuantity=" + itemQuantity +
                ", context='" + context + '\'' +
                '}';
    }

    /**
     * Attempts to parse the supplied {@link String} as a {@link MongoRewardIssuanceId}.
     *
     * @param id the id to parse
     * @return the parsed {@link MongoRewardIssuanceId}
     */
    public static MongoRewardIssuanceId parseOrThrowNotFoundException(final String id) {
        try {
            return new MongoRewardIssuanceId(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ex);
        }
    }

}
