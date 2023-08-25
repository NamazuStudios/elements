package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel
public class UpdateLargeObjectRequest {

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
        UpdateLargeObjectRequest that = (UpdateLargeObjectRequest) o;
        return Objects.equals(getMimeType(), that.getMimeType()) && Objects.equals(getRead(), that.getRead()) && Objects.equals(getWrite(), that.getWrite());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMimeType(), getRead(), getWrite());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateLargeObjectRequest{");
        sb.append("mimeType='").append(mimeType).append('\'');
        sb.append(", read=").append(read);
        sb.append(", write=").append(write);
        sb.append('}');
        return sb.toString();
    }

}
