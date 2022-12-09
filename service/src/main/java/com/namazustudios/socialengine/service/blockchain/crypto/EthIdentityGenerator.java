package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.exception.crypto.CryptoException;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletIdentityPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class EthIdentityGenerator implements WalletIdentityFactory.IdentityGenerator {

    @Override
    public WalletIdentityPair generate() {
        final ECKeyPair ecKeyPair;

        try {
            ecKeyPair = Keys.createEcKeyPair();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new CryptoException(ex);
        }

        final var identity = new WalletIdentityPair();
        final var credentials = Credentials.create(ecKeyPair);

        identity.setEncrypted(false);
        identity.setAddress(credentials.getAddress());
        identity.setPrivateKey(credentials.getEcKeyPair().getPrivateKey().toString(16));

        return identity;

    }

}
