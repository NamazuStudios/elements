package com.namazustudios.socialengine.codeserve.api.deploy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel
public class CreateDeploymentRequest {

    @NotNull
    @ApiModelProperty
    protected Integer version;

    @NotNull
    @ApiModelProperty
    protected String revision;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
}
