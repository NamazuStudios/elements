package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@Entity(value = "application_configuration")
public class MongoFirebaseApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String serviceAccountCredentials;

    public String getServiceAccountCredentials() {
        return serviceAccountCredentials;
    }

    public void setServiceAccountCredentials(String serviceAccountCredentials) {
        this.serviceAccountCredentials = serviceAccountCredentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoFirebaseApplicationConfiguration)) return false;

        MongoFirebaseApplicationConfiguration that = (MongoFirebaseApplicationConfiguration) o;

        return getServiceAccountCredentials() != null ? getServiceAccountCredentials().equals(that.getServiceAccountCredentials()) : that.getServiceAccountCredentials() == null;
    }

    @Override
    public int hashCode() {
        return getServiceAccountCredentials() != null ? getServiceAccountCredentials().hashCode() : 0;
    }

}
