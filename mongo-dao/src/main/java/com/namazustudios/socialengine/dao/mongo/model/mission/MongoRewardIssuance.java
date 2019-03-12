package com.namazustudios.socialengine.dao.mongo.model.mission;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.model.reward.RewardIssuance.Type;
import com.namazustudios.socialengine.model.reward.RewardIssuance.State;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import org.mongodb.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Entity(value = "progress_pending_award", noClassnameStored = true)
public class MongoRewardIssuance {
    @Id
    private MongoRewardIssuanceId objectId;

    @Indexed
    @Reference
    private MongoUser user;

    @Indexed
    private State state;

    @Reference
    private MongoItem item;

    @Property
    private int itemQuantity;

    @Indexed
    private String context;

    @Property
    private Type type;

    @Property
    private String source;

    @Embedded
    private Map<String, Object> metadata;

    @Embedded
    private List<String> tags;

    @Indexed(options = @IndexOptions(expireAfterSeconds = 0))
    private Timestamp expirationTimestamp;

    @Indexed
    private String uuid;

    public MongoRewardIssuanceId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoRewardIssuanceId objectId) {
        this.objectId = objectId;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoItem getItem() {
        return item;
    }

    public void setItem(MongoItem item) {
        this.item = item;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public Timestamp getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(Timestamp expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoRewardIssuance that = (MongoRewardIssuance) o;
        return getItemQuantity() == that.getItemQuantity() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                getState() == that.getState() &&
                Objects.equals(getItem(), that.getItem()) &&
                Objects.equals(getContext(), that.getContext()) &&
                getType() == that.getType() &&
                Objects.equals(getSource(), that.getSource()) &&
                Objects.equals(getMetadata(), that.getMetadata()) &&
                Objects.equals(getTags(), that.getTags()) &&
                Objects.equals(getExpirationTimestamp(), that.getExpirationTimestamp()) &&
                Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId(), getUser(), getState(), getItem(), getItemQuantity(),
                getContext(), getType(), getSource(), getMetadata(), getTags(), getExpirationTimestamp(),
                getUuid());
    }

    @Override
    public String toString() {
        return "MongoRewardIssuance{" +
                "objectId=" + objectId +
                ", user=" + user +
                ", state=" + state +
                ", item=" + item +
                ", itemQuantity=" + itemQuantity +
                ", context='" + context + '\'' +
                ", type=" + type +
                ", source='" + source + '\'' +
                ", metadata=" + metadata +
                ", tags=" + tags +
                ", expirationTimestamp=" + expirationTimestamp +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
