package com.namazustudios.socialengine.model.blockchain.template;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

@ApiModel(description = "Represents a request to create a TokenTemplate definition.")
public class CreateTokenTemplateRequest {

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The token template tabs to create.")
    List<TemplateTab> tabs;

    public List<TemplateTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<TemplateTab> tabs) {
        this.tabs = tabs;
    }
}
