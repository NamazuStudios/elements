package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;

public class SolanaIdentityGenerator implements WalletIdentityFactory.IdentityGenerator {

    @Override
    public WalletAccount generate() {
        throw new NotImplementedException("Not implemented.");
    }

}
