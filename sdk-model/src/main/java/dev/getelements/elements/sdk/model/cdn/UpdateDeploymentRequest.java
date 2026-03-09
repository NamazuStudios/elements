package dev.getelements.elements.sdk.model.cdn;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to update a CDN deployment with a new revision. */
public class UpdateDeploymentRequest {

    /** Creates a new instance. */
    public UpdateDeploymentRequest() {}

    /** The revision identifier for the deployment update. */
    @NotNull
    @Schema
    protected String revision;

    /**
     * Returns the revision for the deployment update.
     *
     * @return the revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the revision for the deployment update.
     *
     * @param revision the revision
     */
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
