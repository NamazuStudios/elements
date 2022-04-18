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

    @ApiModelProperty("The Token Name of the template.")
    private String tokenName;

    @ApiModelProperty("The Contact Id of the template.")
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
