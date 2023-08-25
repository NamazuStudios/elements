package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;

@ApiModel
public class CreateLargeObjectRequest {

    //TODO: discover and add more params here

    @Null
    @ApiModelProperty("The path in the bucket which will hold the object.")
    private String path;

    @NotNull
    @ApiModelProperty("The MIME type associated with the object.")
    private String mimeType;

    @Valid
    @NotNull
    @ApiModelProperty("Specifies the Subjects which can read the LargeObject.")
    private SubjectRequest read;

    @Valid
    @NotNull
    @ApiModelProperty("Specifies the Subjects which can write the LargeObject.")
    private SubjectRequest write;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateLargeObjectRequest that = (CreateLargeObjectRequest) o;
        return Objects.equals(getPath(), that.getPath()) && Objects.equals(getMimeType(), that.getMimeType()) && Objects.equals(getRead(), that.getRead()) && Objects.equals(getWrite(), that.getWrite());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getMimeType(), getRead(), getWrite());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateLargeObjectRequest{");
        sb.append("path='").append(path).append('\'');
        sb.append(", mimeType='").append(mimeType).append('\'');
        sb.append(", read=").append(read);
        sb.append(", write=").append(write);
        sb.append('}');
        return sb.toString();
    }

}
