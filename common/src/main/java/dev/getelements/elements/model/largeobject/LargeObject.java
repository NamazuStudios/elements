package dev.getelements.elements.model.largeobject;

import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;

@ApiModel
public class LargeObject {

    @NotNull(groups = Update.class)
    @Null(groups = {Insert.class, Create.class})
    @ApiModelProperty("The unique ID of the LargeObject.")
    private String id;


    @ApiModelProperty(
            "The URL where the binary contents of the LargeObject may be read. May be null, since Path param or Id is pointer for object."
    )
    private String url;

    @NotNull
    @ApiModelProperty("The path to the file in the underlying bucket.")
    private String path;

    @ApiModelProperty("The MIME type of the LargeObject.")
    private String mimeType;

    @Valid
    @NotNull
    @ApiModelProperty("Permission associated with LargeObject.")
    private AccessPermissions accessPermissions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AccessPermissions getAccessPermissions() {
        return accessPermissions;
    }

    public void setAccessPermissions(AccessPermissions accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        LargeObject that = (LargeObject) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUrl(), that.getUrl()) && Objects.equals(getPath(), that.getPath()) && Objects.equals(getMimeType(), that.getMimeType()) && Objects.equals(getAccessPermissions(), that.getAccessPermissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUrl(), getPath(), getMimeType(), getAccessPermissions());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LargeObject{");
        sb.append("id='").append(id).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", mimeType='").append(mimeType).append('\'');
        sb.append(", accessPermissions=").append(accessPermissions);
        sb.append('}');
        return sb.toString();
    }

}
