package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;

public class ElementsSmartContract {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the contract itself.")
    private String id;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("The name given to this contract.")
    private String displayName;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The script hash of the contract from the blockchain.")
    private String scriptHash;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The blockchain where this contract lives. Valid values are " +
            "\"NEO\" : This contract exists on the NEO blockchain network.")
    private String blockchain;

    @ApiModelProperty("Any meta data for this contract.")
    private Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getScriptHash() {
        return scriptHash;
    }

    public void setScriptHash(String scriptHash) {
        this.scriptHash = scriptHash;
    }

    public String getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(String blockchain) {
        this.blockchain = blockchain;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
