package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@Entity
public class MongoElementServiceReference {

    @Property
    private String elementName;

    @Property
    private String serviceType;

    @Property
    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

}
