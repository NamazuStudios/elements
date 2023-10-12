package dev.getelements.elements.model.profile;

import dev.getelements.elements.model.largeobject.LargeObjectReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(description = "Represents a request to update an image profile.")
public class UpdateProfileImageRequest {

    @ApiModelProperty("Image object stored in EL large objects storage.")
    private LargeObjectReference imageObject;

    //TODO: synchronize with accepted mimetypes
    @ApiModelProperty("MimeType of image")
    private String mimeType;

    public LargeObjectReference getImageObject() {
        return imageObject;
    }

    public void setImageObject(LargeObjectReference imageObject) {
        this.imageObject = imageObject;
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
        return Objects.equals(imageObject, that.imageObject) && Objects.equals(mimeType, that.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageObject, mimeType);
    }

    @Override
    public String toString() {
        return "UpdateProfileImageRequest{" +
                "imageObject=" + imageObject +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
