package dev.getelements.elements.model.blockchain;

import dev.getelements.elements.model.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;

@Deprecated
public class PatchSmartContractRequest {

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("The name given to this contract for display purposes.")
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

    @ApiModelProperty("The Elements database id of the wallet containing the default account to be used for " +
            "contract related requests. If null, a wallet id must be specified in any invocation requests " +
            "(see contract/invoke).")
    private String walletId;

    @ApiModelProperty("The public key/address to be used as the default account for contract related requests. " +
            "If null, the associated wallet must have a single default account. " +
            "If not null, then if the wallet has multiple accounts and none are flagged as default, " +
            "or if the account is not contained within the wallet, then an error will be thrown.")
    private String accountAddress;

    @ApiModelProperty("Any metadata for this contract.")
    private Map<String, Object> metadata;

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

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
