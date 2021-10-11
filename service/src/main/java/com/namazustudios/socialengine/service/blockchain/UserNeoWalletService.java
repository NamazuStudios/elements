package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;

import javax.inject.Inject;

public class UserNeoWalletService implements NeoWalletService {

    private NeoWalletDao neoWalletDao;

    @Override
    public Pagination<SmartContractTemplate> getWallets(int offset, int count, String search) {
        return null;
    }

    @Override
    public NeoWallet getWallet(String walletIdOrName) {
        return null;
    }

    @Override
    public NeoWallet updateWallet(UpdateWalletRequest walletRequest) {
        return null;
    }

    @Override
    public NeoWallet createWallet(CreateWalletRequest walletRequest) {
        return null;
    }

    @Override
    public void deleteWallet(String walletId) {

    }

    public NeoWalletDao getWalletDao() {
        return neoWalletDao;
    }

    @Inject
    public void setWalletDao(NeoWalletDao neoWalletDao) {
        this.neoWalletDao = neoWalletDao;
    }
}
