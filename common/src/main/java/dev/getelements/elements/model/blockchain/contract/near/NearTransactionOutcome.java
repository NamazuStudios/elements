package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class NearTransactionOutcome {
    @ApiModelProperty("proof")
    private List<NearProof> proof;

    @ApiModelProperty("block_hash")
    private NearEncodedHash blockHash;

    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("outcome")
    private NearOutcome outcome;

    public List<NearProof> getProof() {
        return proof;
    }

    public void setProof(List<NearProof> proof) {
        this.proof = proof;
    }

    public NearEncodedHash getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(NearEncodedHash blockHash) {
        this.blockHash = blockHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NearOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(NearOutcome outcome) {
        this.outcome = outcome;
    }
}
