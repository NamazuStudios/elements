package dev.getelements.elements.service.blockchain.crypto;

import com.syntifi.near.api.rpc.service.KeyService;
import dev.getelements.elements.sdk.model.blockchain.wallet.WalletAccount;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletAccountFactory;

public class NearAccountGenerator implements WalletAccountFactory.AccountGenerator {

    @Override
    public WalletAccount generate() {
        final var privateKey = KeyService.deriveRandomKey();
        final var publicKey = KeyService.derivePublicKey(privateKey);
        final var address = publicKey.getJsonPublicKey();
        final var identity = new WalletAccount();

        identity.setEncrypted(false);
        identity.setAddress(address);
        identity.setPrivateKey(privateKey.getJsonPrivateKey());

        return identity;
    }

}