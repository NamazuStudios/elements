package dev.getelements.elements.sdk.model;

import dev.getelements.elements.sdk.model.application.Application;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/** Represents a deployment of a versioned artifact to an application. */
@Schema
public class Deployment implements Serializable {

    /** Creates a new instance. */
    public Deployment() {}

    /** The unique ID of the deployment itself. */
    @NotNull()
    @Schema(description = "The unique ID of the deployment itself.")
    protected String id;

    /** The deployment version. */
    @NotNull
    @Schema(description = "The deployment version. Allows for overriding versions with new content.")
    protected String version;

    /** The revision that this deployment points to. */
    @NotNull
    @Schema(description = "The revision that this deployment points to.")
    protected String revision;

    /** The application this deployment is for. */
    @NotNull
    @Schema(description = "The application this deployment is for.")
    protected Application application;

    /**
     * Returns the unique deployment identifier.
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique deployment identifier.
     * @param id the ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the deployment version.
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the deployment version.
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the revision that this deployment points to.
     * @return the revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the revision that this deployment points to.
     * @param revision the revision
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Returns the application this deployment belongs to.
     * @return the application
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Sets the application this deployment belongs to.
     * @param application the application
     */
    public void setApplication(Application application) {
        this.application = application;
    }
}
