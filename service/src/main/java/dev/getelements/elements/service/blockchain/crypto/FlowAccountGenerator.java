package dev.getelements.elements.service.blockchain.crypto;

import dev.getelements.elements.sdk.model.blockchain.wallet.WalletAccount;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletAccountFactory;
import org.onflow.sdk.crypto.Crypto;

public class FlowAccountGenerator implements WalletAccountFactory.AccountGenerator {

    @Override
    public WalletAccount generate() {
        final var keyPair = Crypto.generateKeyPair();
        final var account = new WalletAccount();
        account.setEncrypted(false);
        account.setAddress(keyPair.getPublic().getHex());
        account.setPrivateKey(keyPair.getPrivate().getHex());
        return account;
    }

}
