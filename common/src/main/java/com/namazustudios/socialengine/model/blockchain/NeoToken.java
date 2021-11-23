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

    @NotNull(groups = ValidationGroups.Create.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @ApiModelProperty("The base token properties used by the blockchain.")
    private Token token;

    @ApiModelProperty("Any meta data for this token.")
    private Map<String, Object> metaData;

    private String contract;

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

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
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
}
