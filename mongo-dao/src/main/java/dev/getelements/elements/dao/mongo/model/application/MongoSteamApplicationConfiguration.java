package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Property;

import java.util.List;

public class MongoSteamApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String publisherKey;

    @Property
    private String appId;

    @Property
    private List<MongoProductBundle> productBundles;

    public String getPublisherKey() {
        return publisherKey;
    }

    public void setPublisherKey(String publisherKey) {
        this.publisherKey = publisherKey;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<MongoProductBundle> getProductBundles() {
        return productBundles;
    }

    public void setProductBundles(List<MongoProductBundle> productBundles) {
        this.productBundles = productBundles;
    }

}
