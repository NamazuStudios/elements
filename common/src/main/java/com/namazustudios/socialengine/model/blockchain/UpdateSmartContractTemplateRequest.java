package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Represents a request to update a Smart Contract Template.")
public class UpdateSmartContractTemplateRequest {

    @ApiModelProperty("The name or id of the template.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private String nameOrId;

    @ApiModelProperty("The compiled smart contract.")
    @NotNull(groups = ValidationGroups.Insert.class)
    private Object templateBinary;

    public String getNameOrId() {
        return nameOrId;
    }

    public void setNameOrId(String nameOrId) {
        this.nameOrId = nameOrId;
    }

    public Object getTemplateBinary() {
        return templateBinary;
    }

    public void setTemplateBinary(Object templateBinary) {
        this.templateBinary = templateBinary;
    }
}
