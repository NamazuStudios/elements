package com.namazustudios.socialengine.model.blockchain;

public class ElementsPurchaseToken extends AbstractElementsToken {

    private Ownership ownership;

    private String transferOptions;

    public Ownership getOwnership() {
        return ownership;
    }

    public void setOwnership(Ownership ownership) {
        this.ownership = ownership;
    }

    public String getTransferOptions() {
        return transferOptions;
    }

    public void setTransferOptions(String transferOptions) {
        this.transferOptions = transferOptions;
    }
}
