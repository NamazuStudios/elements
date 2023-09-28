package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CreateLargeObjectFromUrlRequest extends CreateLargeObjectRequest {

    @NotNull
    @ApiModelProperty("List with image URLs")
    String fileUrl;

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CreateLargeObjectFromUrlRequest that = (CreateLargeObjectFromUrlRequest) o;
        return Objects.equals(fileUrl, that.fileUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fileUrl);
    }
}
