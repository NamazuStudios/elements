package dev.getelements.elements.service.blockchain.crypto;

import dev.getelements.elements.sdk.model.exception.NotImplementedException;
import dev.getelements.elements.sdk.model.blockchain.wallet.WalletAccount;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletAccountFactory;

public class SolanaAccountGenerator implements WalletAccountFactory.AccountGenerator {

    @Override
    public WalletAccount generate() {
        throw new NotImplementedException("Not implemented.");
    }

}
