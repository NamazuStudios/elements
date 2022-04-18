package com.namazustudios.socialengine.model.blockchain.template;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

@ApiModel
public class TokenTemplate {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the template itself.")
    private String id;

    @ApiModelProperty("The Token Name of the template.")
    private String tokenName;

    @ApiModelProperty("The Contact Id of the template.")
    private String contractId;

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The tabs of the token template.")
    private List<TemplateTab> tabs ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        return this.tabs;
    }

    public void setTabs(List<TemplateTab> tabs) {
        this.tabs = tabs;
    }
}
