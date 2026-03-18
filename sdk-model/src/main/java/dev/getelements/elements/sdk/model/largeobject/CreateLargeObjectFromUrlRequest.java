package dev.getelements.elements.sdk.model.largeobject;



import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to create a large object from a remote URL. */
public class CreateLargeObjectFromUrlRequest extends CreateLargeObjectRequest {

    /** Creates a new instance. */
    public CreateLargeObjectFromUrlRequest() {}

    @NotNull
    @Schema(description = "List with image URLs")
    String fileUrl;

    /**
     * Returns the URL of the file to fetch.
     *
     * @return the file URL
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * Sets the URL of the file to fetch.
     *
     * @param fileUrl the file URL
     */
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
