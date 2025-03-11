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

    private String version;

    private String revision;

    private String timestamp;

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

    public String getTimestamp() {
        return timestamp;
    }

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
