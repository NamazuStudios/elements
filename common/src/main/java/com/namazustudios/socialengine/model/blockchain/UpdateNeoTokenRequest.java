package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Represents a request to update a NeoToken.")
public class UpdateNeoTokenRequest {

    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The updated token definition.")
    private Token token;

    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("Is this token listed for sale?")
    private boolean listed;

    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The elements contract id to mint this token with.")
    private String contractId;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean isListed() {
        return listed;
    }

    public void setListed(boolean listed) {
        this.listed = listed;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
}
