package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Property;

import java.util.List;
import java.util.Map;

/**
 * Created by patricktwohig on 5/31/17.
 */
public class MongoGooglePlayApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String applicationId;

    @Property
    private Map<String, Object> jsonKey;

    @Property
    private List<MongoProductBundle> productBundles;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Map<String, Object> getJsonKey() {
        return jsonKey;
    }

    public void setJsonKey(Map<String, Object> jsonKey) {
        this.jsonKey = jsonKey;
    }

    public List<MongoProductBundle> getProductBundles() {
        return productBundles;
    }

    public void setProductBundles(List<MongoProductBundle> productBundles) {
        this.productBundles = productBundles;
    }

}
