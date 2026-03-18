package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Represents a Firebase application configuration. */
@Schema
public class FirebaseApplicationConfiguration extends ApplicationConfiguration {

    /** Creates a new instance. */
    public FirebaseApplicationConfiguration() {}

    @NotNull
    @Schema(description = "The contents of the serviceAccountCredentials.json file.")
    private String projectId;

    @NotNull
    @Schema(description = "The contents of the serviceAccountCredentials.json file.")
    private String serviceAccountCredentials;

    /**
     * Returns the Firebase project identifier.
     * @return the project ID
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Sets the Firebase project identifier.
     * @param projectId the project ID
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * Returns the service account credentials JSON.
     * @return the service account credentials
     */
    public String getServiceAccountCredentials() {
        return serviceAccountCredentials;
    }

    /**
     * Sets the service account credentials JSON.
     * @param serviceAccountCredentials the service account credentials
     */
    public void setServiceAccountCredentials(String serviceAccountCredentials) {
        this.serviceAccountCredentials = serviceAccountCredentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirebaseApplicationConfiguration)) return false;
        if (!super.equals(o)) return false;

        FirebaseApplicationConfiguration that = (FirebaseApplicationConfiguration) o;

        if (getProjectId() != null ? !getProjectId().equals(that.getProjectId()) : that.getProjectId() != null)
            return false;
        return getServiceAccountCredentials() != null ? getServiceAccountCredentials().equals(that.getServiceAccountCredentials()) : that.getServiceAccountCredentials() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getProjectId() != null ? getProjectId().hashCode() : 0);
        result = 31 * result + (getServiceAccountCredentials() != null ? getServiceAccountCredentials().hashCode() : 0);
        return result;
    }

}
