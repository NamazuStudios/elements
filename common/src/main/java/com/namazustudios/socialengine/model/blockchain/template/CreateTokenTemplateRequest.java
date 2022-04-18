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

    @ApiModelProperty("The Token Name of the template.")
    private String tokenName;

    @ApiModelProperty("The contract id of the template. ")
    private String contractId;

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public List<TemplateTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<TemplateTab> tabs) {
        this.tabs = tabs;
    }
}
