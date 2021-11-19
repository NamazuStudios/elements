package com.namazustudios.socialengine.model.blockchain;

public class ElementsLicenseToken extends AbstractElementsToken{

    private boolean revocable;

    private long expiry;

    private boolean renewable;

    public boolean isRevocable() {
        return revocable;
    }

    public void setRevocable(boolean revocable) {
        this.revocable = revocable;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }
}
