package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

import java.util.List;

/**
 * Created by patricktwohig on 5/31/17.
 */
@Entity(value = "application_configuration")
public class MongoIosApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String applicationId;

    @Property
    private List<MongoProductBundle> productBundles;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public List<MongoProductBundle> getProductBundles() {
        return productBundles;
    }

    public void setProductBundles(List<MongoProductBundle> productBundles) {
        this.productBundles = productBundles;
    }

}
