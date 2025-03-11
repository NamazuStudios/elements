package dev.getelements.elements.sdk.model.largeobject;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema
public class UpdateLargeObjectRequest {

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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public SubjectRequest getRead() {
        return read;
    }

    public void setRead(SubjectRequest read) {
        this.read = read;
    }

    public SubjectRequest getWrite() {
        return write;
    }

    public void setWrite(SubjectRequest write) {
        this.write = write;
    }

    public SubjectRequest getDelete() {
        return delete;
    }

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
