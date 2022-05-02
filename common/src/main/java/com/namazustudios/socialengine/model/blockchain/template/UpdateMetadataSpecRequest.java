package com.namazustudios.socialengine.model.blockchain.template;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "Represents a request to update a MetadataSpec.")
public class UpdateMetadataSpecRequest {
    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The updated token template tabs.")
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
