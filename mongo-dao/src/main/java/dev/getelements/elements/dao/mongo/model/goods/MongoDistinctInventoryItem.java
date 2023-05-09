package dev.getelements.elements.dao.mongo.model.goods;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Map;

@Entity(value = "distinct_inventory_items", useDiscriminator = false)
@Indexes({
    @Index(fields = @Field(value = "item")),
    @Index(fields = @Field(value = "user")),
    @Index(fields = @Field(value = "profile")),
})
public class MongoDistinctInventoryItem {

    @Id
    private ObjectId objectId;

    @Reference
    private MongoUser user;

    @Reference
    private MongoProfile profile;

    @Reference
    private MongoItem item;

    @Property
    private Map<String, Object> metadata;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public MongoItem getItem() {
        return item;
    }

    public void setItem(MongoItem item) {
        this.item = item;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

}
