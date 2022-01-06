package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

@ApiModel
public class NeoToken {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the token itself.")
    private String id;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @ApiModelProperty("The base token properties used by the blockchain.")
    private Token token;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The elements contract id to mint this token with.")
    private String contractId;

    private boolean listed;

    private boolean minted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public boolean isMinted() {
        return minted;
    }

    public void setMinted(boolean minted) {
        this.minted = minted;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
}
