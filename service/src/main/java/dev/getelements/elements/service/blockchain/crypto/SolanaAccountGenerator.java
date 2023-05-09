package dev.getelements.elements.service.blockchain.crypto;

import dev.getelements.elements.exception.NotImplementedException;
import dev.getelements.elements.model.blockchain.wallet.WalletAccount;

public class SolanaAccountGenerator implements WalletAccountFactory.AccountGenerator {

    @Override
    public WalletAccount generate() {
        throw new NotImplementedException("Not implemented.");
    }

}
