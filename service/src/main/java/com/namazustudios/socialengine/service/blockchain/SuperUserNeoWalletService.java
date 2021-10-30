package com.namazustudios.socialengine.service.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.exception.security.InsufficientPermissionException;
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

public class SuperUserNeoWalletService implements NeoWalletService {

    private NeoWalletDao neoWalletDao;

    private User user;

    private PasswordGenerator passwordGenerator;

    private Neow3jService neow3jService;

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
        var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();
        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        }
        var pw = Strings.nullToEmpty(walletRequest.getPassword()).trim();

        if (pw.isEmpty()){
            var wallet = getNeow3jService().createWallet(walletRequest.getDisplayName());
            var neoWallet = new NeoWallet();

            neoWallet.displayName = walletRequest.getDisplayName();
            neoWallet.wallet = wallet;
            neoWallet.setUser(user);

            return neoWalletDao.createWallet(neoWallet);
        } else {
            try {
                var wallet = getNeow3jService().createWallet(walletRequest.getDisplayName(), pw);
                var neoWallet = new NeoWallet();

                neoWallet.displayName = walletRequest.getDisplayName();
                neoWallet.wallet = wallet;
                neoWallet.setUser(user);

                return neoWalletDao.createWallet(neoWallet);
            } catch (CipherException e) {
                return null;
            }
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

    public Neow3jService getNeow3jService(){return neow3jService;}

    @Inject
    public void setNeow3jService(Neow3jService neow3jService){this.neow3jService = neow3jService;}
}
