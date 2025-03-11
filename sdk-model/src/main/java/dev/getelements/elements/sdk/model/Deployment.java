package dev.getelements.elements.sdk.model;

import dev.getelements.elements.sdk.model.application.Application;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Schema
public class Deployment implements Serializable {

    @NotNull()
    @Schema(description = "The unique ID of the deployment itself.")
    protected String id;

    @NotNull
    @Schema(description = "The deployment version. Allows for overriding versions with new content.")
    protected String version;

    @NotNull
    @Schema(description = "The revision that this deployment points to.")
    protected String revision;

    @NotNull
    @Schema(description = "The application this deployment is for.")
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
