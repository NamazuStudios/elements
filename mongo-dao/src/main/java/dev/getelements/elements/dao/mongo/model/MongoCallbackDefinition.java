package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@Entity
public class MongoCallbackDefinition {

    @Property
    private String module;

    @Property
    private String method;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
