package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Represents the ownership structure of a blockchain token, including stakeholders and capitalization. */
public class Ownership {

    /** Creates a new instance. */
    public Ownership() {}

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The list of stakeholders that will be assigned when minting this token.")
    private List<StakeHolder> stakeHolders;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The total number of shares allocated to this token.")
    private long capitalization;

    /**
     * Returns the list of stakeholders assigned to this token.
     *
     * @return the stakeholders
     */
    public List<StakeHolder> getStakeHolders() {
        return stakeHolders;
    }

    /**
     * Sets the list of stakeholders assigned to this token.
     *
     * @param stakeHolders the stakeholders
     */
    public void setStakeHolders(List<StakeHolder> stakeHolders) {
        this.stakeHolders = stakeHolders;
    }

    /**
     * Returns the total number of shares allocated to this token.
     *
     * @return the capitalization
     */
    public long getCapitalization() {
        return capitalization;
    }

    /**
     * Sets the total number of shares allocated to this token.
     *
     * @param capitalization the capitalization
     */
    public void setCapitalization(long capitalization) {
        this.capitalization = capitalization;
    }
}
