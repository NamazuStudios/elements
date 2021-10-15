package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.PasswordGenerator;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.wallet.Wallet;

import javax.inject.Inject;
import java.util.Optional;

public class UserNeoWalletService implements NeoWalletService {

    private NeoWalletDao neoWalletDao;

    private User user;

    private PasswordGenerator passwordGenerator;

    @Override
    public Pagination<NeoWallet> getWallets(int offset, int count, String search) {
        return getWalletDao().getWallets(offset, count, search);
    }

    @Override
    public Optional<NeoWallet> getWallet(String walletIdOrName) {
        return getWalletDao().getWallet(walletIdOrName);
    }

    @Override
    public NeoWallet updateWallet(UpdateWalletRequest walletRequest) {
        return getWalletDao().updateWallet(walletRequest);
    }

    @Override
    public NeoWallet createWallet(CreateWalletRequest walletRequest) {

        var user = getUser();
        var pw = getPasswordGenerator().generate();

        try {
            var wallet = Wallet.create(pw)
                    .name(walletRequest.getDisplayName())
                    .toNEP6Wallet();

            var neoWallet = new NeoWallet();

            neoWallet.displayName = walletRequest.getDisplayName();
            neoWallet.wallet = wallet;

            return neoWalletDao.createWallet(neoWallet);

        } catch (CipherException e) {
            return null;
        }
    }

    @Override
    public void deleteWallet(String walletId) {
        getWalletDao().deleteWallet(walletId);
    }

    public NeoWalletDao getWalletDao() {
        return neoWalletDao;
    }

    @Inject
    public void setWalletDao(NeoWalletDao neoWalletDao) {
        this.neoWalletDao = neoWalletDao;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public PasswordGenerator getPasswordGenerator() {
        return passwordGenerator;
    }

    @Inject
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }
}
