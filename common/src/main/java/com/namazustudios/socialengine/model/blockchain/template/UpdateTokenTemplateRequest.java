package com.namazustudios.socialengine.model.blockchain.template;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.Token;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

@ApiModel(description = "Represents a request to update a TokenTemplate.")
public class UpdateTokenTemplateRequest {
    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The updated token template tabs.")
    List<TemplateTab> tabs;

    public List<TemplateTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<TemplateTab> tabs) {
        this.tabs = tabs;
    }
}
