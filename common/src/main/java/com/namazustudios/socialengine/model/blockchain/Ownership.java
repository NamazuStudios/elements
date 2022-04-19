package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

public class Ownership {

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @ApiModelProperty("The list of stakeholders that will be assigned when minting this token.")
    private List<StakeHolder> stakeHolders;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @ApiModelProperty("The total number of shares allocated to this token.")
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
