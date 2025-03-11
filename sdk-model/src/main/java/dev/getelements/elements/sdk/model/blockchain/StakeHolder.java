package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

public class StakeHolder {

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The account address of the stakeholder to be assigned when minting this token.")
    private String owner;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "If true, allows for voting on any proposed change.")
    private boolean voting;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The number of shares assigned to the Stakeholder.")
    private long shares;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isVoting() {
        return voting;
    }

    public void setVoting(boolean voting) {
        this.voting = voting;
    }

    public long getShares() {
        return shares;
    }

    public void setShares(long shares) {
        this.shares = shares;
    }
}
