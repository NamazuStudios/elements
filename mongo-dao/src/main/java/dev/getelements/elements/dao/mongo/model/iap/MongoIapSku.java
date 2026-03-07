package dev.getelements.elements.dao.mongo.model.iap;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;

@Entity(value = "iap_sku", useDiscriminator = false)
@Indexes({
        @Index(fields = {@Field("schema"), @Field("productId")}, options = @IndexOptions(unique = true))
})
public class MongoIapSku {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed
    private String schema;

    @Property
    @Indexed
    private String productId;

    @Property
    private List<MongoIapSkuReward> rewards;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public List<MongoIapSkuReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<MongoIapSkuReward> rewards) {
        this.rewards = rewards;
    }

}
