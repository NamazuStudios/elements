package com.namazustudios.socialengine.model.blockchain;

import io.swagger.annotations.ApiModelProperty;

public class Royalties {

    @ApiModelProperty("Determines whether the API should automatically convert percentages to reflect a cap of " +
            "10,000, or whether the user has input share numbers and a custom cap.")
    private boolean advanced;

    @ApiModelProperty("Is shareholder voting allowed?")
    private boolean voting;

    private String name;

    private String publicKey;

    private long percentage;

    private long shares;

    private long totalCap;

    public boolean isAdvanced() {
        return advanced;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public boolean isVoting() {
        return voting;
    }

    public void setVoting(boolean voting) {
        this.voting = voting;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public long getPercentageOrShares() {
        if (isAdvanced()) {
            return shares;
        }
        return percentage;
    }

    public void setPercentageOrShares(long percentageOrShares) {
        if (isAdvanced()) {
            shares = percentageOrShares;
        } else {
            percentage = percentageOrShares;
        }
    }

    public long getShares() {
        return shares;
    }

    public void setShares(long shares) {
        this.shares = shares;
    }

    public long getTotalCap() {
        if (isAdvanced()) {
            return totalCap;
        }
        return 10000;
    }

    public void setTotalCap(long totalCap) {
        this.totalCap = totalCap;
    }
}
