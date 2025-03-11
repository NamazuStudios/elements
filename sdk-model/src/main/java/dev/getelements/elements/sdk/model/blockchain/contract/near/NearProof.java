package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

public class NearProof {
    public enum Direction {
        RIGHT("Right"),
        LEFT("Left");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Schema(description = "hash")
    private NearEncodedHash hash;

    @Schema(description = "direction")
    private Direction direction;

    public NearEncodedHash getHash() {
        return hash;
    }

    public void setHash(NearEncodedHash hash) {
        this.hash = hash;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
