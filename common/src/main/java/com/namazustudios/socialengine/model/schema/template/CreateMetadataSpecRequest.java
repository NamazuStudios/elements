package com.namazustudios.socialengine.model.schema.template;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

@ApiModel(description = "Represents a request to create a MetadataSpec definition.")
public class CreateMetadataSpecRequest {

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The token template tabs to create.")
    List<TemplateTab> tabs;

    @ApiModelProperty("The name of the schema.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TemplateTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<TemplateTab> tabs) {
        this.tabs = tabs;
    }
}
