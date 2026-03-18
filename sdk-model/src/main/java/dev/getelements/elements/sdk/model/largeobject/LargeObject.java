package dev.getelements.elements.sdk.model.largeobject;

import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.Date;
import java.util.Objects;

/** Represents a large binary object stored in the system. */
@Schema
public class LargeObject {

    @NotNull(groups = Update.class)
    @Null(groups = {Insert.class, Create.class})
    @Schema(description = "The unique ID of the LargeObject.")
    private String id;


    @Schema(description =
            "The URL where the binary contents of the LargeObject may be read. May be null, since Path param or Id is pointer for object."
    )
    private String url;

    @NotNull
    @Schema(description = "The path to the file in the underlying bucket.")
    private String path;

    @Schema(description = "The MIME type of the LargeObject.")
    private String mimeType;

    @Valid
    @NotNull
    @Schema(description = "Permission associated with LargeObject.")
    private AccessPermissions accessPermissions;

    @Schema(description = "Current state of large object.")
    private LargeObjectState state;

    @Schema(description = "Date of last modification.")
    private Date lastModified;

    @Schema(description = "The original name of the file.")
    private String originalFilename;

    /** Creates a new instance with state set to EMPTY. */
    public LargeObject() {
        this.state = LargeObjectState.EMPTY;
    }

    /**
     * Returns the unique ID of the large object.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the large object.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the access permissions for this large object.
     *
     * @return the access permissions
     */
    public AccessPermissions getAccessPermissions() {
        return accessPermissions;
    }

    /**
     * Sets the access permissions for this large object.
     *
     * @param accessPermissions the access permissions
     */
    public void setAccessPermissions(AccessPermissions accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    /**
     * Returns the URL where the binary contents may be read.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL where the binary contents may be read.
     *
     * @param url the url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the path to the file in the underlying bucket.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path to the file in the underlying bucket.
     *
     * @param path the path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the MIME type of the large object.
     *
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the MIME type of the large object.
     *
     * @param mimeType the MIME type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the current state of the large object.
     *
     * @return the state
     */
    public LargeObjectState getState() {
        return state;
    }

    /**
     * Sets the current state of the large object.
     *
     * @param state the state
     */
    public void setState(LargeObjectState state) {
        this.state = state;
    }

    /**
     * Returns the date of last modification.
     *
     * @return the last modified date
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the date of last modification.
     *
     * @param lastModified the last modified date
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Returns the original name of the file.
     *
     * @return the original filename
     */
    public String getOriginalFilename() {
        return originalFilename;
    }

    /**
     * Sets the original name of the file.
     *
     * @param originalFilename the original filename
     */
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LargeObject that = (LargeObject) o;
        return Objects.equals(id, that.id) && Objects.equals(url, that.url) && Objects.equals(path, that.path) && Objects.equals(mimeType, that.mimeType) && Objects.equals(accessPermissions, that.accessPermissions) && state == that.state && Objects.equals(lastModified, that.lastModified) && Objects.equals(originalFilename, that.originalFilename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, path, mimeType, accessPermissions, state, lastModified, originalFilename);
    }

    @Override
    public String toString() {
        return "LargeObject{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", path='" + path + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", accessPermissions=" + accessPermissions +
                ", state=" + state +
                ", lastModified=" + lastModified +
                ", originalFilename='" + originalFilename + '\'' +
                '}';
    }
}
