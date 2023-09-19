package dev.getelements.elements.service.blockchain.crypto;

import com.syntifi.near.api.rpc.service.KeyService;
import dev.getelements.elements.model.blockchain.wallet.WalletAccount;
import org.apache.commons.codec.binary.Hex;

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