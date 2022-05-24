package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.security.InsufficientPermissionException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.PasswordGenerator;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.web3j.crypto.CipherException;

public class UserBscWalletService implements BscWalletService {

    private BscWalletDao bscWalletDao;

    private User user;

    private PasswordGenerator passwordGenerator;

    private Bscw3jClient bscw3JClient;

    @Override
    public Pagination<BscWallet> getWallets(int offset, int count, String search) {
        return getWalletDao().getWallets(offset, count, search);
    }

    @Override
    public BscWallet getWallet(String walletId) {
        return getWalletDao().getWallet(walletId);
    }

    @Override
    public BscWallet updateWallet(String walletId, UpdateBscWalletRequest walletRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();
        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        } else if(!user.getId().equals(userId)){
            throw new InsufficientPermissionException("You do not have permission to update a wallet for another user.");
        }

        var name = Strings.nullToEmpty(walletRequest.getDisplayName()).trim();
        var password = Strings.nullToEmpty(walletRequest.getPassword()).trim();
        var newPassword = Strings.nullToEmpty(walletRequest.getNewPassword()).trim();

        var bscWallet = getWalletDao().getWallet(walletId);
        try {
            var wallet = getBscw3jClient().updateWallet(bscWallet.getWallet(), name, password, newPassword);
            return getWalletDao().updateWallet(walletId, walletRequest, wallet);
        } catch (CipherException  | JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }
    }

    @Override
    public BscWallet createWallet(CreateBscWalletRequest walletRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();
        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        } else if(!user.getId().equals(userId)){
            throw new InsufficientPermissionException("You do not have permission to create a wallet for another user.");
        }
        var pw = Strings.nullToEmpty(walletRequest.getPassword()).trim();

        var existing = getWalletDao().getWalletForUser(walletRequest.getUserId(), walletRequest.getDisplayName());
        if (existing != null) {
            throw new DuplicateException(String.format("Wallet with name: %s already exists.", walletRequest.getDisplayName()));
        }

        if (pw.isEmpty()){
            try {
                var wallet = getBscw3jClient().createWallet(walletRequest.getDisplayName());
                var bscWallet = new BscWallet();

                bscWallet.setDisplayName(walletRequest.getDisplayName());
                bscWallet.setWallet(wallet);
                bscWallet.setUser(user);

                return bscWalletDao.createWallet(bscWallet);
            } catch ( CipherException | JsonProcessingException e) {
                throw new InternalException(e.getMessage());
            }
        } else {
            try {
                var wallet = getBscw3jClient().createWallet(walletRequest.getDisplayName(), pw);
                var bscWallet = new BscWallet();

                bscWallet.setDisplayName(walletRequest.getDisplayName());
                bscWallet.setWallet(wallet);
                bscWallet.setUser(user);

                return bscWalletDao.createWallet(bscWallet);
            } catch (CipherException | JsonProcessingException e) {
                throw new InternalException(e.getMessage());
            }
        }
    }

    @Override
    public void deleteWallet(String walletId) {
        getWalletDao().deleteWallet(walletId);
    }

    public BscWalletDao getWalletDao() {
        return bscWalletDao;
    }

    @Inject
    public void setWalletDao(BscWalletDao bscWalletDao) {
        this.bscWalletDao = bscWalletDao;
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

    public Bscw3jClient getBscw3jClient(){return bscw3JClient;}

    @Inject
    public void setBscw3jClient(Bscw3jClient bscw3JClient){this.bscw3JClient = bscw3JClient;}
}
