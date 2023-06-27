package dev.getelements.elements.service.blockchain.crypto;

import dev.getelements.elements.model.blockchain.wallet.WalletAccount;
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
