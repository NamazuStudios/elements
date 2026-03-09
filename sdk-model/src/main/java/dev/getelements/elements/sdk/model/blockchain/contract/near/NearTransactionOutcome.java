package dev.getelements.elements.sdk.model.blockchain.contract.near;



import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Represents the outcome of a NEAR protocol transaction execution. */
public class NearTransactionOutcome {

    /** Creates a new instance. */
    public NearTransactionOutcome() {}

    @Schema(description = "proof")
    private List<NearProof> proof;

    @Schema(description = "block_hash")
    private NearEncodedHash blockHash;

    @Schema(description = "id")
    private String id;

    @Schema(description = "outcome")
    private NearOutcome outcome;

    /**
     * Returns the Merkle proof for this transaction outcome.
     *
     * @return the proof
     */
    public List<NearProof> getProof() {
        return proof;
    }

    /**
     * Sets the Merkle proof for this transaction outcome.
     *
     * @param proof the proof
     */
    public void setProof(List<NearProof> proof) {
        this.proof = proof;
    }

    /**
     * Returns the hash of the block containing this transaction.
     *
     * @return the block hash
     */
    public NearEncodedHash getBlockHash() {
        return blockHash;
    }

    /**
     * Sets the hash of the block containing this transaction.
     *
     * @param blockHash the block hash
     */
    public void setBlockHash(NearEncodedHash blockHash) {
        this.blockHash = blockHash;
    }

    /**
     * Returns the ID of this transaction.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of this transaction.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the execution outcome of this transaction.
     *
     * @return the outcome
     */
    public NearOutcome getOutcome() {
        return outcome;
    }

    /**
     * Sets the execution outcome of this transaction.
     *
     * @param outcome the outcome
     */
    public void setOutcome(NearOutcome outcome) {
        this.outcome = outcome;
    }
}
