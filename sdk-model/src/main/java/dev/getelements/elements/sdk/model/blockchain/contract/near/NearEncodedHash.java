package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a base58-encoded hash used in the NEAR protocol. */
public class NearEncodedHash {

    /** Creates a new instance. */
    public NearEncodedHash() {}

    @Schema
    private String encodedHash;

    /**
     * Returns the base58-encoded hash string.
     *
     * @return the encoded hash
     */
    public String getEncodedHash() {
        return encodedHash;
    }

    /**
     * Sets the base58-encoded hash string.
     *
     * @param encodedHash the encoded hash
     */
    public void setEncodedHash(String encodedHash) {
        this.encodedHash = encodedHash;
    }

}
