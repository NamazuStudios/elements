package dev.getelements.elements.sdk.model.blockchain.contract.near;



import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class NearTransactionOutcome {
    @Schema(description = "proof")
    private List<NearProof> proof;

    @Schema(description = "block_hash")
    private NearEncodedHash blockHash;

    @Schema(description = "id")
    private String id;

    @Schema(description = "outcome")
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
