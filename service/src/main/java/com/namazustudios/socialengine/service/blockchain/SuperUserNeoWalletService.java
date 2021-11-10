package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.security.InsufficientPermissionException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.service.UserService;
import com.namazustudios.socialengine.service.user.UserUserService;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Wallet;

import javax.inject.Inject;
import java.util.Optional;

public class SuperUserNeoWalletService implements NeoWalletService {

    private NeoWalletDao neoWalletDao;

    private User user;

    private PasswordGenerator passwordGenerator;

    private Neow3jService neow3jService;

    private UserService userService;

    @Override
    public Pagination<NeoWallet> getWallets(int offset, int count, String userId) {
        return getWalletDao().getWallets(offset, count, userId);
    }

    @Override
    public Optional<NeoWallet> getWallet(String walletId) {
        return getWalletDao().getWallet(walletId);
    }

    @Override
    public NeoWallet updateWallet(String walletId, UpdateWalletRequest walletRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();
        if (userId.isEmpty()){
            userId = user.getId();
        }

        var name = Strings.nullToEmpty(walletRequest.getDisplayName()).trim();
        var password = Strings.nullToEmpty(walletRequest.getPassword()).trim();
        var newPassword = Strings.nullToEmpty(walletRequest.getNewPassword()).trim();

        NEP6Wallet updatedWallet = null;
        var neoWallet = getWalletDao().getWallet(walletId);
        if(neoWallet.isPresent()) {
            try {
                updatedWallet = getNeow3jService().updateWallet(neoWallet.get().getWallet(), name, password, newPassword);
            } catch (CipherException | NEP2InvalidFormat | NEP2InvalidPassphrase e) {
                return null;
            }
        }

        try {
            return getWalletDao().updateWallet(walletId, userId, updatedWallet);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public NeoWallet createWallet(CreateWalletRequest walletRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();
        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        }
        var pw = Strings.nullToEmpty(walletRequest.getPassword()).trim();

        var existing = getWalletDao().getWalletForUser(walletRequest.getUserId(), walletRequest.getDisplayName());
        if (existing != null) {
            throw new DuplicateException(String.format("Wallet with name: %s already exists.", walletRequest.getDisplayName()));
        }

        if (pw.isEmpty()){
            try {
                var wallet = getNeow3jService().createWallet(walletRequest.getDisplayName());
                var neoWallet = new NeoWallet();

                neoWallet.setDisplayName(walletRequest.getDisplayName());
                neoWallet.setWallet(wallet);
                neoWallet.setUser(getUserService().getUser(walletRequest.getUserId()));

                return getWalletDao().createWallet(neoWallet);
            } catch (CipherException | JsonProcessingException e) {
                return null;
            }
        } else {
            try {
                var wallet = getNeow3jService().createWallet(walletRequest.getDisplayName(), pw);
                var neoWallet = new NeoWallet();

                neoWallet.setDisplayName(walletRequest.getDisplayName());
                neoWallet.setWallet(wallet);
                neoWallet.setUser(getUserService().getUser(walletRequest.getUserId()));

                return neoWalletDao.createWallet(neoWallet);
            } catch (CipherException | JsonProcessingException e) {
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

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
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
