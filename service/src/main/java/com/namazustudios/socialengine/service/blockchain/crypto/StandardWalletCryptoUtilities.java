package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;

import java.util.Optional;

public class StandardWalletCryptoUtilities implements WalletCryptoUtilities {

    @Override
    public Wallet encrypt(final Wallet wallet, String passphrase) {
        return null;
    }

    @Override
    public Optional<Wallet> decrypt(final Wallet wallet, String passphrase) {
        return Optional.empty();
    }

}
