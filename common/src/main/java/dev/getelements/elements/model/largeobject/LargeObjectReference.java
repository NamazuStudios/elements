package dev.getelements.elements.model.largeobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@ApiModel
public class LargeObjectReference implements Serializable {

    @ApiModelProperty("The unique ID of the LargeObject.")
    private String id;

    @ApiModelProperty("The URL where the binary contents of the LargeObject may be read.")
    private String url;

    @ApiModelProperty("The MIME type of the LargeObject.")
    private String mimeType;

    @ApiModelProperty("Current state of the LargeObject.")
    private LargeObjectState state;

    @ApiModelProperty("Date of last modification")
    private Date lastModified;

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

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public LargeObjectState getState() {
        return state;
    }

    public void setState(LargeObjectState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LargeObjectReference reference = (LargeObjectReference) o;
        return Objects.equals(id, reference.id) && Objects.equals(url, reference.url) && Objects.equals(mimeType, reference.mimeType) && state == reference.state && Objects.equals(lastModified, reference.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, mimeType, state, lastModified);
    }

    @Override
    public String toString() {
        return "LargeObjectReference{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", state=" + state +
                ", lastModified=" + lastModified +
                '}';
    }
}
