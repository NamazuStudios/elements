package com.namazustudios.socialengine.cdnserve.api;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

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
}
