package dev.getelements.elements.sdk.model.largeobject;

import io.swagger.v3.oas.annotations.media.Schema;


import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/** Represents a reference to a large object, containing its key metadata. */
@Schema
public class LargeObjectReference implements Serializable {

    /** Creates a new instance. */
    public LargeObjectReference() {}

    @Schema(description = "The unique ID of the LargeObject.")
    private String id;

    @Schema(description = "The URL where the binary contents of the LargeObject may be read.")
    private String url;

    @Schema(description = "The MIME type of the LargeObject.")
    private String mimeType;

    @Schema(description = "Current state of the LargeObject.")
    private LargeObjectState state;

    @Schema(description = "Date of last modification")
    private Date lastModified;

    /**
     * Creates a {@link LargeObjectReference} from the {@link LargeObject}.
     *
     * @param largeObject the large object
     * @return the new {@link LargeObjectReference}
     */
    public static LargeObjectReference fromLargeObject(LargeObject largeObject) {
        final var largeObjectReference = new LargeObjectReference();
        largeObjectReference.id = largeObject.getId();
        largeObjectReference.url = largeObject.getUrl();
        largeObjectReference.mimeType = largeObject.getMimeType();
        largeObjectReference.state = largeObject.getState();
        largeObjectReference.lastModified = largeObject.getLastModified();
        return largeObjectReference;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LargeObjectReference reference = (LargeObjectReference) o;
        return Objects.equals(id, reference.id) && Objects.equals(url, reference.url) && Objects.equals(mimeType, reference.mimeType) && state == reference.state && Objects.equals(lastModified, reference.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, mimeType, state, lastModified);
    }

    @Override
    public String toString() {
        return "LargeObjectReference{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", state=" + state +
                ", lastModified=" + lastModified +
                '}';
    }
}
