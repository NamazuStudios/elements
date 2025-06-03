package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.dao.mongo.model.application.MongoElementServiceReference;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@Entity
public class MongoCallbackDefinition {

    @Property
    private String method;

    @Property
    private MongoElementServiceReference service;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public MongoElementServiceReference getService() {
        return service;
    }

    public void setService(MongoElementServiceReference service) {
        this.service = service;
    }

}
