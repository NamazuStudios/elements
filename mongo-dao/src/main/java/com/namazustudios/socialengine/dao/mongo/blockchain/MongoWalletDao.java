package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.Wallet;

public class MongoWalletDao implements WalletDao {

    @Override
    public Pagination<SmartContractTemplate> getWallets(int offset, int count, String search) {
        return null;
    }

    @Override
    public Wallet getWallet(String walletIdOrName) {
        return null;
    }

    @Override
    public Wallet updateWallet(UpdateWalletRequest walletRequest) {
        return null;
    }

    @Override
    public Wallet createWallet(CreateWalletRequest walletRequest) {
        return null;
    }

    @Override
    public void deleteWallet(String walletId) {

    }
}
