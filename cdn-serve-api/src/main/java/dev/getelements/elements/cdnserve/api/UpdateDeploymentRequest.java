package dev.getelements.elements.cdnserve.api;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class UpdateDeploymentRequest {

    @NotNull
    @ApiModelProperty
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
