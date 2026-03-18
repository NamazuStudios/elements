package dev.getelements.elements.sdk.model.cdn;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to create a new CDN deployment. */
@Schema
public class CreateDeploymentRequest {

    /** Creates a new instance. */
    public CreateDeploymentRequest() {}

    /** The deployment version. */
    @NotNull
    @Schema
    protected String version;

    /** The deployment revision. */
    @NotNull
    @Schema
    protected String revision;

    /**
     * Returns the deployment version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the deployment version.
     *
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the deployment revision.
     *
     * @return the revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the deployment revision.
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
        CreateDeploymentRequest that = (CreateDeploymentRequest) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(revision, that.revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, revision);
    }
}
