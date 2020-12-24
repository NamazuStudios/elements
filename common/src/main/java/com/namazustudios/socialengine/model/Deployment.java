package com.namazustudios.socialengine.model;

import com.namazustudios.socialengine.model.application.Application;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel
public class Deployment implements Serializable {

    @NotNull()
    @ApiModelProperty("The unique ID of the deployment itself.")
    protected String id;

    @NotNull
    @ApiModelProperty("The deployment version. Allows for overriding versions with new content.")
    protected String version;

    @NotNull
    @ApiModelProperty("The revision that this deployment points to.")
    protected String revision;

    @NotNull
    @ApiModelProperty("The application this deployment is for.")
    protected Application application;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
