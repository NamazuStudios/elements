package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.neow3j.types.Hash160;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;

public class UpdateNeoSmartContractRequest {

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("The name given to this contract.")
    private String displayName;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The script hash of the contract from the blockchain.")
    private Hash160 scriptHash;

    @ApiModelProperty("Any meta data for this contract.")
    private Map<String, Object> metadata;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Hash160 getScriptHash() {
        return scriptHash;
    }

    public void setScriptHash(Hash160 scriptHash) {
        this.scriptHash = scriptHash;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
