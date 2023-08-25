package dev.getelements.elements.model.largeobject;

import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ApiModel
public class LargeObject {

    @NotNull(groups = Update.class)
    @Null(groups = {Insert.class, Create.class})
    @ApiModelProperty("The unique ID of the LargeObject.")
    private String id;

    @NotNull
    @ApiModelProperty("Permission associated with LargeObject.")
    private AccessPermissions accessPermissions;

    @NotNull
    @ApiModelProperty("LargeObject URL")
    private String url;

    @NotNull
    @ApiModelProperty("LargeObject URL")
    private String mimeType;

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
