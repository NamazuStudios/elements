package com.namazustudios.socialengine.model.blockchain;

import org.web3j.abi.datatypes.generated.Uint160;

public class StakeHolder {

    private boolean voting;

    private Uint160 owner;

    private long shares;

    public boolean isVoting() {
        return voting;
    }

    public void setVoting(boolean voting) {
        this.voting = voting;
    }

    public Uint160 getOwner() {
        return owner;
    }

    public void setOwner(Uint160 owner) {
        this.owner = owner;
    }

    public long getShares() {
        return shares;
    }

    public void setShares(long shares) {
        this.shares = shares;
    }
}
