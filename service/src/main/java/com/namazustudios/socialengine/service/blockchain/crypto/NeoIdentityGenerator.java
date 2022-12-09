package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.model.blockchain.wallet.WalletIdentityPair;
import com.namazustudios.socialengine.service.blockchain.crypto.WalletIdentityFactory.IdentityGenerator;
import io.neow3j.wallet.Wallet;

public class NeoIdentityGenerator implements IdentityGenerator {

    @Override
    public WalletIdentityPair generate() {

        final var identity = new WalletIdentityPair();
        final var wallet = Wallet.create();
        final var account = wallet.getAccounts().get(0);

        identity.setEncrypted(false);
        identity.setAddress(account.getAddress());
        identity.setPrivateKey(account.getECKeyPair().getPrivateKey().getInt().toString(16));

        return identity;

    }

}
