package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class CreateLargeObjectRequest {

    @NotNull
    @ApiModelProperty("The MIME type associated with the object.")
    private String mimeType;

    @Valid
    @ApiModelProperty("Specifies the Subjects which can read the LargeObject.")
    private SubjectRequest read;

    @Valid
    @ApiModelProperty("Specifies the Subjects which can write the LargeObject.")
    private SubjectRequest write;

    @Valid
    @ApiModelProperty("Specifies the Subjects which can delete the LargeObject.")
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
