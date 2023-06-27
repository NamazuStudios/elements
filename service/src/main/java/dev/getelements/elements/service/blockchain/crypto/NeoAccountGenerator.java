package dev.getelements.elements.service.blockchain.crypto;

import dev.getelements.elements.model.blockchain.wallet.WalletAccount;
import dev.getelements.elements.service.blockchain.crypto.WalletAccountFactory.AccountGenerator;
import io.neow3j.wallet.Wallet;

public class NeoAccountGenerator implements AccountGenerator {

    @Override
    public WalletAccount generate() {

        final var identity = new WalletAccount();
        final var wallet = Wallet.create();
        final var account = wallet.getAccounts().get(0);

        identity.setEncrypted(false);
        identity.setAddress(account.getAddress());
        identity.setPrivateKey(account.getECKeyPair().getPrivateKey().getInt().toString(16));

        return identity;

    }

}
