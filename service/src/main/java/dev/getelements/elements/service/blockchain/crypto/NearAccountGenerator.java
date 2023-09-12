package dev.getelements.elements.service.blockchain.crypto;

import com.syntifi.near.api.rpc.service.KeyService;
import dev.getelements.elements.model.blockchain.wallet.WalletAccount;
import org.apache.commons.codec.binary.Hex;

public class NearAccountGenerator implements WalletAccountFactory.AccountGenerator {

    @Override
    public WalletAccount generate() {
        final var privateKey = KeyService.deriveRandomKey();
        final var publicKey = KeyService.derivePublicKey(privateKey);

        //Near addresses are public keys encoded to hex
        //See https://docs.near.org/concepts/basics/accounts/creating-accounts
        final var address = Hex.encodeHexString(publicKey.getData());

        final var identity = new WalletAccount();
        identity.setEncrypted(false);
        identity.setAddress(address);
        identity.setPrivateKey(privateKey.getJsonPrivateKey());

        return identity;
    }

}