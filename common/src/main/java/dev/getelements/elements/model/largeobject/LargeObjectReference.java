package dev.getelements.elements.model.largeobject;

import dev.getelements.elements.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;

@ApiModel
public class LargeObjectReference {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the LargeObject.")
    private String id;

//TODO: null / notnull rules.
    @ApiModelProperty("The URL where the binary contents of the LargeObject may be read.")
    private String url;

    @NotNull
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
