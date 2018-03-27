package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

@SearchableDocument
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoFirebaseApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String serviceAccountKey;

    public String getServiceAccountKey() {
        return serviceAccountKey;
    }

    public void setServiceAccountKey(String serviceAccountKey) {
        this.serviceAccountKey = serviceAccountKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoFirebaseApplicationConfiguration)) return false;

        MongoFirebaseApplicationConfiguration that = (MongoFirebaseApplicationConfiguration) o;

        return getServiceAccountKey() != null ? getServiceAccountKey().equals(that.getServiceAccountKey()) : that.getServiceAccountKey() == null;
    }

    @Override
    public int hashCode() {
        return getServiceAccountKey() != null ? getServiceAccountKey().hashCode() : 0;
    }

}
