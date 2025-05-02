package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

import java.util.List;
import java.util.Map;

/**
 * Created by patricktwohig on 5/31/17.
 */
@Entity(value = "application_configuration")
public class MongoGooglePlayApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private Map<String, Object> jsonKey;

    @Property
    private List<MongoProductBundle> productBundles;

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
