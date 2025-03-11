package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.List;

public class BulkMintTokenRequest {

    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The unique ID of the contract to mint the tokens with.")
    private String contractId;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The unique ID's of the tokens to mint.")
    private List<String> tokenIds;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The public address of the account with funds to mint.")
    private String address;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public List<String> getTokenIds() {
        return tokenIds;
    }

    public void setTokenIds(List<String> tokenIds) {
        this.tokenIds = tokenIds;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
