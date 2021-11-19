package com.namazustudios.socialengine.model.blockchain;

import java.util.List;

public class Ownership {

    private List<StakeHolder> stakeHolders;

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
