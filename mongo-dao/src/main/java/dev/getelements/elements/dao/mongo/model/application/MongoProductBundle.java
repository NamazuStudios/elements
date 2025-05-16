package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.*;


@Embedded
public class MongoProductBundle {

    @Property
    private String productId;

    @Property
    private String displayName;

    @Property
    private String description;

    @Property
    private List<MongoProductBundleReward> productBundleRewards = new ArrayList<>();

    @Property
    private Map<String, Object> metadata = new HashMap<>();

    @Property
    private boolean display;

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

    public Boolean getDisplay() {
        return display;
    }

    public void setDisplay(Boolean display) {
        this.display = display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoProductBundle that = (MongoProductBundle) o;
        return Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getDisplayName(), that.getDisplayName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getProductBundleRewards(), that.getProductBundleRewards()) &&
                Objects.equals(getMetadata(), that.getMetadata()) &&
                Objects.equals(getDisplay(), that.getDisplay());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductId(), getDisplayName(), getDescription(), getProductBundleRewards(),
                getMetadata(), getDisplay());
    }
}
