package dev.getelements.elements.sdk.model.largeobject;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to update a large object's MIME type and access control subjects. */
@Schema
public class UpdateLargeObjectRequest {

    /** Creates a new instance. */
    public UpdateLargeObjectRequest() {}

    @NotNull
    @Schema(description = "The MIME type associated with the object.")
    private String mimeType;

    @Valid
    @NotNull
    @Schema(description = "Specifies the Subjects which can read the LargeObject.")
    private SubjectRequest read;

    @Valid
    @NotNull
    @Schema(description = "Specifies the Subjects which can write the LargeObject.")
    private SubjectRequest write;

    @Valid
    @NotNull
    @Schema(description = "Specifies the Subjects which can delete the LargeObject.")
    private SubjectRequest delete;

    /**
     * Returns the MIME type associated with the object.
     *
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the MIME type associated with the object.
     *
     * @param mimeType the MIME type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the subjects which can read the large object.
     *
     * @return the read subjects
     */
    public SubjectRequest getRead() {
        return read;
    }

    /**
     * Sets the subjects which can read the large object.
     *
     * @param read the read subjects
     */
    public void setRead(SubjectRequest read) {
        this.read = read;
    }

    /**
     * Returns the subjects which can write the large object.
     *
     * @return the write subjects
     */
    public SubjectRequest getWrite() {
        return write;
    }

    /**
     * Sets the subjects which can write the large object.
     *
     * @param write the write subjects
     */
    public void setWrite(SubjectRequest write) {
        this.write = write;
    }

    /**
     * Returns the subjects which can delete the large object.
     *
     * @return the delete subjects
     */
    public SubjectRequest getDelete() {
        return delete;
    }

    /**
     * Sets the subjects which can delete the large object.
     *
     * @param delete the delete subjects
     */
    public void setDelete(SubjectRequest delete) {
        this.delete = delete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateLargeObjectRequest that = (UpdateLargeObjectRequest) o;
        return Objects.equals(mimeType, that.mimeType) && Objects.equals(read, that.read) && Objects.equals(write, that.write) && Objects.equals(delete, that.delete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mimeType, read, write, delete);
    }

    @Override
    public String toString() {
        return "UpdateLargeObjectRequest{" +
                "mimeType='" + mimeType + '\'' +
                ", read=" + read +
                ", write=" + write +
                ", delete=" + delete +
                '}';
    }
}
