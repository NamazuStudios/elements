package com.namazustudios.socialengine.model.blockchain;

public class StakeHolder {

    private String owner;

    private boolean voting;

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
