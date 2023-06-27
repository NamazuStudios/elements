package dev.getelements.elements.model.blockchain.neo;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.BlockchainConstants.MintStatus;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.blockchain.Token;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@ApiModel
public class NeoToken {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the token itself.")
    private String id;

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique blockchain ID of the token.")
    private String tokenUUID;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @ApiModelProperty("The base token properties used by the blockchain.")
    private Token token;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The elements contract id to mint this token with.")
    private String contractId;

    @Null(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @ApiModelProperty("The unique id of th series the token belongs to.")
    private String seriesId;

    @Null(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @ApiModelProperty("How many instances of this token have been minted.")
    private long totalMintedQuantity;

    @ApiModelProperty("Whether this token is listed for sale.")
    private boolean listed;

    @ApiModelProperty("The minting status of this token.")
    private MintStatus mintStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTokenUUID() {
        return tokenUUID;
    }

    public void setTokenUUID(String tokenUUID) {
        this.tokenUUID = tokenUUID;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public long getTotalMintedQuantity() {
        return totalMintedQuantity;
    }

    public void setTotalMintedQuantity(long totalMintedQuantity) {
        this.totalMintedQuantity = totalMintedQuantity;
    }

    public boolean isListed() {
        return listed;
    }

    public void setListed(boolean listed) {
        this.listed = listed;
    }

    public MintStatus getMintStatus() {
        return mintStatus;
    }

    public void setMintStatus(MintStatus mintStatus) {
        this.mintStatus = mintStatus;
    }

}
