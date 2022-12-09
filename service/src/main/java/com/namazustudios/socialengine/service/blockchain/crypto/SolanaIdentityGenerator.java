package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletIdentityPair;

public class SolanaIdentityGenerator implements WalletIdentityFactory.IdentityGenerator {

    @Override
    public WalletIdentityPair generate() {
        throw new NotImplementedException("Not implemented.");
    }

}
