package dev.getelements.elements.dao.mongo.model.blockchain;

import dev.getelements.elements.sdk.model.blockchain.wallet.WalletAccount;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

@Embedded
public class MongoWalletAccount {

    @Property
    private String address;

    @Property
    private String privateKey;

    @Property
    private boolean encrypted;

    public MongoWalletAccount() { }

    public MongoWalletAccount(final WalletAccount pair) {
        address = pair.getAddress();
        privateKey = pair.getPrivateKey();
        encrypted = pair.isEncrypted();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

}
