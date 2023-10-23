package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel
public class LargeObjectReference implements Serializable {

    @ApiModelProperty("The unique ID of the LargeObject.")
    private String id;

    @ApiModelProperty("The URL where the binary contents of the LargeObject may be read.")
    private String url;

    @ApiModelProperty("The MIME type of the LargeObject.")
    private String mimeType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        LargeObjectReference that = (LargeObjectReference) o;
        return Objects.equals(id, that.id) && Objects.equals(url, that.url) && Objects.equals(mimeType, that.mimeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, mimeType);
    }

    @Override
    public String toString() {
        return "LargeObjectReference{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}
