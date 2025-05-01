package dev.getelements.elements.dao.mongo.model.application;

import dev.getelements.elements.sdk.model.application.ConfigurationCategory;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Indexes({
        @Index(
                fields = {@Field("name") },
                options = @IndexOptions(unique = true, sparse = true)
        ),
        @Index(
                fields = {@Field("category"), @Field("parent"), @Field("name") },
                options = @IndexOptions(unique = true)
        ),
})
@Entity(value = "application_configuration")
public class MongoApplicationConfiguration {

    @Id
    private ObjectId objectId;

    @Property("name")
    private String uniqueIdentifier;

    @Reference("parent")
    private MongoApplication parent;

    @Property("productBundles")
    private List<MongoProductBundle> productBundles = new ArrayList<>();

    @Property("category")
    private ConfigurationCategory category;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public MongoApplication getParent() {
        return parent;
    }

    public void setParent(MongoApplication parent) {
        this.parent = parent;
    }

    public List<MongoProductBundle> getProductBundles() {
        return productBundles;
    }

    public void setProductBundles(List<MongoProductBundle> productBundles) {
        this.productBundles = productBundles;
    }

    public ConfigurationCategory getCategory() {
        return category;
    }

    public void setCategory(ConfigurationCategory category) {
        this.category = category;
    }

}
