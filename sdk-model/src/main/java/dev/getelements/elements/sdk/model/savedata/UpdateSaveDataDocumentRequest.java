package dev.getelements.elements.sdk.model.savedata;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Represents a request to update a save data document, including the contents and versioning information.
 */
@Schema(description = "Updates a save data document. This accepts the contents of the document as well as the " +
                      "versioning information required to take the update properly.")
public class UpdateSaveDataDocumentRequest {

    /** Creates a new instance. */
    public UpdateSaveDataDocumentRequest() {}

    private Boolean force;

    private String version;

    @NotNull
    private String contents;

    /**
     * Returns whether to force the update regardless of version conflicts.
     *
     * @return true if the update should be forced
     */
    public Boolean getForce() {
        return force;
    }

    /**
     * Sets whether to force the update regardless of version conflicts.
     *
     * @param force true if the update should be forced
     */
    public void setForce(Boolean force) {
        this.force = force;
    }

    /**
     * Returns the version identifier for optimistic locking.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version identifier for optimistic locking.
     *
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the contents of the save data document.
     *
     * @return the contents
     */
    public String getContents() {
        return contents;
    }

    /**
     * Sets the contents of the save data document.
     *
     * @param contents the contents
     */
    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateSaveDataDocumentRequest that = (UpdateSaveDataDocumentRequest) o;
        return Objects.equals(getForce(), that.getForce()) && Objects.equals(getVersion(), that.getVersion()) && Objects.equals(getContents(), that.getContents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForce(), getVersion(), getContents());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateSaveDataDocument{");
        sb.append("force=").append(force);
        sb.append(", version='").append(version).append('\'');
        sb.append(", contents='").append(contents).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
