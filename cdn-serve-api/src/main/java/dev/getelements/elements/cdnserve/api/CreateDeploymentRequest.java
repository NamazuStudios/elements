package dev.getelements.elements.cdnserve.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class CreateDeploymentRequest {

    @NotNull
    @ApiModelProperty
    protected String version;

    @NotNull
    @ApiModelProperty
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
