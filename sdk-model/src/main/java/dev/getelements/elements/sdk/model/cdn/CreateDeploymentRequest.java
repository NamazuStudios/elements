package dev.getelements.elements.sdk.model.cdn;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema
public class CreateDeploymentRequest {

    @NotNull
    @Schema
    protected String version;

    @NotNull
    @Schema
    protected String revision;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

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
        CreateDeploymentRequest that = (CreateDeploymentRequest) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(revision, that.revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, revision);
    }
}
