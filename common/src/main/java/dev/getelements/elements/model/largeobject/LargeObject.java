package dev.getelements.elements.model.largeobject;

import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ApiModel
public class LargeObject {

    @NotNull(groups = Update.class)
    @Null(groups = {Insert.class, Create.class})
    @ApiModelProperty("The unique ID of the LargeObject.")
    private String id;

    @Null
    @ApiModelProperty(
            "The URL where the binary contents of the LargeObject may be read. This field is always set by the " +
            "LargeObjectService to indicate where the file resides. A subsequent GET request from the URL will " +
            "fetch the contents of the LargeObject."
    )
    private String url;

    @NotNull
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}
