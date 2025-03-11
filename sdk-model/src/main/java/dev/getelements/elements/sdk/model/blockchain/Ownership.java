package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class Ownership {

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The list of stakeholders that will be assigned when minting this token.")
    private List<StakeHolder> stakeHolders;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "The total number of shares allocated to this token.")
    private long capitalization;

    public List<StakeHolder> getStakeHolders() {
        return stakeHolders;
    }

    public void setStakeHolders(List<StakeHolder> stakeHolders) {
        this.stakeHolders = stakeHolders;
    }

    public long getCapitalization() {
        return capitalization;
    }

    public void setCapitalization(long capitalization) {
        this.capitalization = capitalization;
    }
}
