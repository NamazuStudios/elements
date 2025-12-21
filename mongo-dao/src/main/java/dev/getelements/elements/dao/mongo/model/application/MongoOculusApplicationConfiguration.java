package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Property;

import java.util.List;

public class MongoOculusApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String applicationId;

    @Property
    private String applicationSecret;

    @Property
    private List<String> builtinApplicationPermissions;

    @Property
    private List<MongoProductBundle> productBundles;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public List<String> getBuiltinApplicationPermissions() {
        return builtinApplicationPermissions;
    }

    public void setBuiltinApplicationPermissions(List<String> builtinApplicationPermissions) {
        this.builtinApplicationPermissions = builtinApplicationPermissions;
    }

    public List<MongoProductBundle> getProductBundles() {
        return productBundles;
    }

    public void setProductBundles(List<MongoProductBundle> productBundles) {
        this.productBundles = productBundles;
    }
}