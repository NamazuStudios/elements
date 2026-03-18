package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/** Represents a stakeholder in a blockchain token, holding shares and optionally voting rights. */
public class StakeHolder {

    /** Creates a new instance. */
    public StakeHolder() {}

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The account address of the stakeholder to be assigned when minting this token.")
    private String owner;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "If true, allows for voting on any proposed change.")
    private boolean voting;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The number of shares assigned to the Stakeholder.")
    private long shares;

    /**
     * Returns the account address of the stakeholder.
     *
     * @return the owner address
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the account address of the stakeholder.
     *
     * @param owner the owner address
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Returns whether this stakeholder has voting rights.
     *
     * @return true if voting is allowed
     */
    public boolean isVoting() {
        return voting;
    }

    /**
     * Sets whether this stakeholder has voting rights.
     *
     * @param voting true if voting is allowed
     */
    public void setVoting(boolean voting) {
        this.voting = voting;
    }

    /**
     * Returns the number of shares assigned to this stakeholder.
     *
     * @return the shares
     */
    public long getShares() {
        return shares;
    }

    /**
     * Sets the number of shares assigned to this stakeholder.
     *
     * @param shares the shares
     */
    public void setShares(long shares) {
        this.shares = shares;
    }
}
