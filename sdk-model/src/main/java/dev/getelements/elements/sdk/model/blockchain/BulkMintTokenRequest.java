package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.List;

/** Represents a request to mint multiple blockchain tokens using a specific contract. */
public class BulkMintTokenRequest {

    /** Creates a new instance. */
    public BulkMintTokenRequest() {}

    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The unique ID of the contract to mint the tokens with.")
    private String contractId;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The unique ID's of the tokens to mint.")
    private List<String> tokenIds;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The public address of the account with funds to mint.")
    private String address;

    /**
     * Returns the contract ID to use for minting.
     *
     * @return the contract ID
     */
    public String getContractId() {
        return contractId;
    }

    /**
     * Sets the contract ID to use for minting.
     *
     * @param contractId the contract ID
     */
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    /**
     * Returns the list of token IDs to mint.
     *
     * @return the token IDs
     */
    public List<String> getTokenIds() {
        return tokenIds;
    }

    /**
     * Sets the list of token IDs to mint.
     *
     * @param tokenIds the token IDs
     */
    public void setTokenIds(List<String> tokenIds) {
        this.tokenIds = tokenIds;
    }

    /**
     * Returns the public address of the account with funds to mint.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the public address of the account with funds to mint.
     *
     * @param address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }
}
