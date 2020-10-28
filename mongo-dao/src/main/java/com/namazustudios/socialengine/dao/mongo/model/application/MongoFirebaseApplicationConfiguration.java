package com.namazustudios.socialengine.dao.mongo.model.application;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@SearchableDocument
@Entity(value = "application_configuration", noClassnameStored = true)
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
