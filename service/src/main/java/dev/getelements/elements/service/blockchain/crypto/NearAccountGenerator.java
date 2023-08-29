package dev.getelements.elements.service.blockchain.crypto;

import com.syntifi.near.api.rpc.service.KeyService;
import dev.getelements.elements.model.blockchain.wallet.WalletAccount;

public class NearAccountGenerator implements WalletAccountFactory.AccountGenerator {

    @Override
    public WalletAccount generate() {
        final var privateKey = KeyService.deriveRandomKey();
        final var publicKey = KeyService.derivePublicKey(privateKey);

        final var identity = new WalletAccount();

        identity.setEncrypted(false);
        identity.setAddress(publicKey.getJsonPublicKey());
        identity.setPrivateKey(privateKey.getJsonPrivateKey());

        return identity;

    }

}