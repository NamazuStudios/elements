package dev.getelements.elements.sdk.model.largeobject;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to create a large object with access permissions. */
@Schema
public class CreateLargeObjectRequest {

    /** Creates a new instance. */
    public CreateLargeObjectRequest() {}

    @NotNull
    @Schema(description = "The MIME type associated with the object.")
    private String mimeType;

    @NotNull
    @Valid
    @Schema(description = "Specifies the Subjects which can read the LargeObject.")
    private SubjectRequest read;

    @NotNull
    @Valid
    @Schema(description = "Specifies the Subjects which can write the LargeObject.")
    private SubjectRequest write;

    @NotNull
    @Valid
    @Schema(description = "Specifies the Subjects which can delete the LargeObject.")
    private SubjectRequest delete;

    /**
     * Returns the MIME type of the object.
     *
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the MIME type of the object.
     *
     * @param mimeType the MIME type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the read access subjects.
     *
     * @return the read subjects
     */
    public SubjectRequest getRead() {
        return read;
    }

    /**
     * Sets the read access subjects.
     *
     * @param read the read subjects
     */
    public void setRead(SubjectRequest read) {
        this.read = read;
    }

    /**
     * Returns the write access subjects.
     *
     * @return the write subjects
     */
    public SubjectRequest getWrite() {
        return write;
    }

    /**
     * Sets the write access subjects.
     *
     * @param write the write subjects
     */
    public void setWrite(SubjectRequest write) {
        this.write = write;
    }

    /**
     * Returns the delete access subjects.
     *
     * @return the delete subjects
     */
    public SubjectRequest getDelete() {
        return delete;
    }

    /**
     * Sets the delete access subjects.
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
        CreateLargeObjectRequest that = (CreateLargeObjectRequest) o;
        return Objects.equals(mimeType, that.mimeType) && Objects.equals(read, that.read) && Objects.equals(write, that.write) && Objects.equals(delete, that.delete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mimeType, read, write, delete);
    }

    @Override
    public String toString() {
        return "CreateLargeObjectRequest{" +
                "mimeType='" + mimeType + '\'' +
                ", read=" + read +
                ", write=" + write +
                ", delete=" + delete +
                '}';
    }
}
