package dev.getelements.elements.dao.mongo.model.application;

import dev.getelements.elements.sdk.model.application.ConfigurationCategory;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Indexes({
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

    @Property("active")
    private boolean active;

    @Property("signInPrivateKey")
    private String appleSignInPrivateKey;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAppleSignInPrivateKey() {
        return appleSignInPrivateKey;
    }

    public void setAppleSignInPrivateKey(String appleSignInPrivateKey) {
        this.appleSignInPrivateKey = appleSignInPrivateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoApplicationConfiguration that = (MongoApplicationConfiguration) o;
        return isActive() == that.isActive() &&
                Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getUniqueIdentifier(), that.getUniqueIdentifier()) &&
                Objects.equals(getParent(), that.getParent()) &&
                Objects.equals(getProductBundles(), that.getProductBundles()) &&
                getCategory() == that.getCategory() &&
                Objects.equals(getAppleSignInPrivateKey(), that.getAppleSignInPrivateKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getObjectId(),
            getUniqueIdentifier(),
            getParent(),
            getProductBundles(),
            getCategory(),
            isActive(),
            getAppleSignInPrivateKey()
        );
    }

}
