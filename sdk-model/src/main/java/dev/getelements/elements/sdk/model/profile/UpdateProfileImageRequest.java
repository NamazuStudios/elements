package dev.getelements.elements.sdk.model.profile;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to update a profile image. */
@Schema(description = "Represents a request to update an image profile.")
public class UpdateProfileImageRequest {

    /** Creates a new instance. */
    public UpdateProfileImageRequest() {}

    @NotNull
    @Schema(description = "MimeType of image")
    private String mimeType;

    /**
     * Returns the MIME type of the image.
     *
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the MIME type of the image.
     *
     * @param mimeType the MIME type
     */
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
