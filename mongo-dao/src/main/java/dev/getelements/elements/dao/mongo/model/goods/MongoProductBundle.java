package dev.getelements.elements.dao.mongo.model.goods;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Entity(value = "product_bundle", useDiscriminator = false)
@Indexes({
        @Index(
                fields = {@Field("application"), @Field("schema"), @Field("productId")},
                options = @IndexOptions(unique = true)
        )
})
public class MongoProductBundle {

    @Id
    private ObjectId objectId;

    @Property
    private MongoApplication application;

    @Property
    private String schema;

    @Property
    private String productId;

    @Property
    private String displayName;

    @Property
    private String description;

    @Property
    private List<MongoProductBundleReward> productBundleRewards;

    @Property
    private Map<String, Object> metadata;

    @Property
    private boolean display;

    @Property
    private List<String> tags;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public MongoApplication getApplication() {
        return application;
    }

    public void setApplication(MongoApplication application) {
        this.application = application;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MongoProductBundleReward> getProductBundleRewards() {
        return productBundleRewards;
    }

    public void setProductBundleRewards(List<MongoProductBundleReward> productBundleRewards) {
        this.productBundleRewards = productBundleRewards;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
