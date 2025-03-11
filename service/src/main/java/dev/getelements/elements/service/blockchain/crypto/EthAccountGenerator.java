package dev.getelements.elements.service.blockchain.crypto;

import dev.getelements.elements.sdk.model.exception.crypto.CryptoException;
import dev.getelements.elements.sdk.model.blockchain.wallet.WalletAccount;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletAccountFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class EthAccountGenerator implements WalletAccountFactory.AccountGenerator {

    @Override
    public WalletAccount generate() {
        final ECKeyPair ecKeyPair;

        try {
            ecKeyPair = Keys.createEcKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new CryptoException(ex);
        }

        final var identity = new WalletAccount();
        final var credentials = Credentials.create(ecKeyPair);

        identity.setEncrypted(false);
        identity.setAddress(credentials.getAddress());
        identity.setPrivateKey(credentials.getEcKeyPair().getPrivateKey().toString(16));

        return identity;

    }

}
