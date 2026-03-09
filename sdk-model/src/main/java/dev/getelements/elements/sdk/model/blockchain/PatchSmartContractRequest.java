package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.Map;

/**
 * Request to create or update a smart contract definition.
 *
 * @deprecated use the new smart contract API instead
 */
@Deprecated
public class PatchSmartContractRequest {

    /** Creates a new instance. */
    public PatchSmartContractRequest() {}

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The name given to this contract for display purposes.")
    private String displayName;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The script hash of the contract from the blockchain.")
    private String scriptHash;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The blockchain where this contract lives. Valid values are " +
            "\"NEO\" : This contract exists on the NEO blockchain network.")
    private String blockchain;

    @Schema(description = "The Elements database id of the wallet containing the default account to be used for " +
            "contract related requests. If null, a wallet id must be specified in any invocation requests " +
            "(see contract/invoke).")
    private String walletId;

    @Schema(description = "The public key/address to be used as the default account for contract related requests. " +
            "If null, the associated wallet must have a single default account. " +
            "If not null, then if the wallet has multiple accounts and none are flagged as default, " +
            "or if the account is not contained within the wallet, then an error will be thrown.")
    private String accountAddress;

    @Schema(description = "Any metadata for this contract.")
    private Map<String, Object> metadata;

    /**
     * Returns the display name of the contract.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the contract.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the script hash of the contract from the blockchain.
     *
     * @return the script hash
     */
    public String getScriptHash() {
        return scriptHash;
    }

    /**
     * Sets the script hash of the contract from the blockchain.
     *
     * @param scriptHash the script hash
     */
    public void setScriptHash(String scriptHash) {
        this.scriptHash = scriptHash;
    }

    /**
     * Returns the blockchain where this contract lives.
     *
     * @return the blockchain
     */
    public String getBlockchain() {
        return blockchain;
    }

    /**
     * Sets the blockchain where this contract lives.
     *
     * @param blockchain the blockchain
     */
    public void setBlockchain(String blockchain) {
        this.blockchain = blockchain;
    }

    /**
     * Returns the wallet ID used for contract-related requests.
     *
     * @return the wallet ID
     */
    public String getWalletId() {
        return walletId;
    }

    /**
     * Sets the wallet ID used for contract-related requests.
     *
     * @param walletId the wallet ID
     */
    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    /**
     * Returns the account address used as the default for contract requests.
     *
     * @return the account address
     */
    public String getAccountAddress() {
        return accountAddress;
    }

    /**
     * Sets the account address used as the default for contract requests.
     *
     * @param accountAddress the account address
     */
    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    /**
     * Returns the metadata for this contract.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata for this contract.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
