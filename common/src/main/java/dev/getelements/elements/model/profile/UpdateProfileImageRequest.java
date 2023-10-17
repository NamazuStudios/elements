package dev.getelements.elements.model.profile;

import dev.getelements.elements.model.largeobject.LargeObjectReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(description = "Represents a request to update an image profile.")
public class UpdateProfileImageRequest {

    @ApiModelProperty("Image object stored in EL large objects storage.")
    private LargeObjectReference imageObjectReference;

    //TODO: synchronize with accepted mimetypes
    @ApiModelProperty("MimeType of image")
    private String mimeType;

    public LargeObjectReference getImageObjectReference() {
        return imageObjectReference;
    }

    public void setImageObjectReference(LargeObjectReference imageObjectReference) {
        this.imageObjectReference = imageObjectReference;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateProfileImageRequest that = (UpdateProfileImageRequest) o;
        return Objects.equals(imageObjectReference, that.imageObjectReference) && Objects.equals(mimeType, that.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageObjectReference, mimeType);
    }

    @Override
    public String toString() {
        return "UpdateProfileImageRequest{" +
                "imageObject=" + imageObjectReference +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
