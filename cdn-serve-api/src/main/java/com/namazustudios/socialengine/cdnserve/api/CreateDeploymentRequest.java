package com.namazustudios.socialengine.cdnserve.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

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
}
