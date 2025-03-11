package dev.getelements.elements.sdk.model.cdn;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class UpdateDeploymentRequest {

    @NotNull
    @Schema
    protected String revision;

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateDeploymentRequest that = (UpdateDeploymentRequest) o;
        return Objects.equals(revision, that.revision);
    }

    @Override
    public int hashCode() {
        return getRevision() != null ? getRevision().hashCode() : 0;
    }
}
