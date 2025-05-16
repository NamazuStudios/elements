package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@Entity(value = "application_configuration")
public class MongoFirebaseApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String projectId;

    @Property
    private String serviceAccountCredentials;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getServiceAccountCredentials() {
        return serviceAccountCredentials;
    }

    public void setServiceAccountCredentials(String serviceAccountCredentials) {
        this.serviceAccountCredentials = serviceAccountCredentials;
    }

}
