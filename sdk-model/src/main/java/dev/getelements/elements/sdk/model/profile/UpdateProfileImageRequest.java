package dev.getelements.elements.sdk.model.profile;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema(description = "Represents a request to update an image profile.")
public class UpdateProfileImageRequest {

    @NotNull
    @Schema(description = "MimeType of image")
    private String mimeType;

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
        return Objects.equals(mimeType, that.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mimeType);
    }

    @Override
    public String toString() {
        return "UpdateProfileImageRequest{" +
                "mimeType='" + mimeType + '\'' +
                '}';
    }
}
