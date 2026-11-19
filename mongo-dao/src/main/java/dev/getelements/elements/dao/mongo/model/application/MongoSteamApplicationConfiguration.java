package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Property;

import java.util.List;

public class MongoSteamApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String publisherKey;

    @Property
    private String appId;

    /**
     * True to verify purchases against Steam's sandbox micro-transaction API (ISteamMicroTxnSandbox).
     * Required for team/development purchases: they are recorded only in the sandbox until the
     * application is released on Steam.
     */
    @Property
    private Boolean sandbox;

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

    public Boolean getSandbox() {
        return sandbox;
    }

    public void setSandbox(Boolean sandbox) {
        this.sandbox = sandbox;
    }

    public List<MongoProductBundle> getProductBundles() {
        return productBundles;
    }

    public void setProductBundles(List<MongoProductBundle> productBundles) {
        this.productBundles = productBundles;
    }

}
