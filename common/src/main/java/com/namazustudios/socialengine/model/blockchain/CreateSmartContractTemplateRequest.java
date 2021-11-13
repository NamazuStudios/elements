package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Represents a request to create a Smart Contract Template for an Application.")
public class CreateSmartContractTemplateRequest {

    @ApiModelProperty("The name of this template.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String name;

    @ApiModelProperty("The name or id of the application to associate this template with.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String applicationNameOrId;

    @ApiModelProperty("The compiled smart contract.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private Object templateBinary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationNameOrId() {
        return applicationNameOrId;
    }

    public void setApplicationNameOrId(String applicationNameOrId) {
        this.applicationNameOrId = applicationNameOrId;
    }

    public Object getTemplateBinary() {
        return templateBinary;
    }

    public void setTemplateBinary(Object templateBinary) {
        this.templateBinary = templateBinary;
    }

}
