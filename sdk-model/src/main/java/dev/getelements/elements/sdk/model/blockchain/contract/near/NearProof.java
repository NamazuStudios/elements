package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a Merkle proof entry in a NEAR protocol transaction. */
public class NearProof {

    /** Creates a new instance. */
    public NearProof() {}

    /** Represents the direction of a Merkle proof node. */
    public enum Direction {
        /** Right direction. */
        RIGHT("Right"),
        /** Left direction. */
        LEFT("Left");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        /**
         * Returns the display name of this direction.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }
    }

    @Schema(description = "hash")
    private NearEncodedHash hash;

    @Schema(description = "direction")
    private Direction direction;

    /**
     * Returns the hash of this proof node.
     *
     * @return the hash
     */
    public NearEncodedHash getHash() {
        return hash;
    }

    /**
     * Sets the hash of this proof node.
     *
     * @param hash the hash
     */
    public void setHash(NearEncodedHash hash) {
        this.hash = hash;
    }

    /**
     * Returns the direction of this proof node.
     *
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the direction of this proof node.
     *
     * @param direction the direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
