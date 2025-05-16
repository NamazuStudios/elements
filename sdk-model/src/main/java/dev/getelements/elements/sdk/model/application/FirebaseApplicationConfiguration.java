package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema
public class FirebaseApplicationConfiguration extends ApplicationConfiguration {

    @NotNull
    @Schema(description = "The contents of the serviceAccountCredentials.json file.")
    private String projectId;

    @NotNull
    @Schema(description = "The contents of the serviceAccountCredentials.json file.")
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
