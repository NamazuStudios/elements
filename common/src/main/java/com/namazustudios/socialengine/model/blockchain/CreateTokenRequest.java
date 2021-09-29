package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Represents a request to create a Smart Contract Template for an Application.")
public class CreateTokenRequest {

    @ApiModelProperty("The name of this token.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String name;

    @ApiModelProperty("The id of the smart contract template to deploy.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String templateId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

}
