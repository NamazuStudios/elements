package com.namazustudios.socialengine.service.blockchain.crypto;

import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;

public class SolanaAccountGenerator implements WalletAccountFactory.AccountGenerator {

    @Override
    public WalletAccount generate() {
        throw new NotImplementedException("Not implemented.");
    }

}
