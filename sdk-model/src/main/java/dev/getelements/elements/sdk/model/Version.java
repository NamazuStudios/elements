package dev.getelements.elements.sdk.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

import static java.lang.String.format;

/**
 * Provides simple version information about the running service.
 *
 * Created by patricktwohig on 7/14/17.
 */
@Schema
public class Version implements Serializable {

    /** Creates a new instance. */
    public Version() {}

    private String version;

    private String revision;

    private String timestamp;

    /**
     * Returns the version string.
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version string.
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the revision identifier.
     * @return the revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the revision identifier.
     * @param revision the revision
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Returns the build timestamp.
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the build timestamp.
     * @param timestamp the timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return format("%s - %s - %s", getVersion(), getRevision(), getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;

        Version version1 = (Version) o;

        if (getVersion() != null ? !getVersion().equals(version1.getVersion()) : version1.getVersion() != null)
            return false;
        if (getRevision() != null ? !getRevision().equals(version1.getRevision()) : version1.getRevision() != null)
            return false;
        return getTimestamp() != null ? getTimestamp().equals(version1.getTimestamp()) : version1.getTimestamp() == null;
    }

    @Override
    public int hashCode() {
        int result = getVersion() != null ? getVersion().hashCode() : 0;
        result = 31 * result + (getRevision() != null ? getRevision().hashCode() : 0);
        result = 31 * result + (getTimestamp() != null ? getTimestamp().hashCode() : 0);
        return result;
    }

}
