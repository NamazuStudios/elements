package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.neo.CreateNeoWalletRequest;
import com.namazustudios.socialengine.model.blockchain.neo.NeoWallet;
import com.namazustudios.socialengine.model.blockchain.neo.UpdateNeoWalletRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.service.UserService;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;

import javax.inject.Inject;

public class SuperUserNeoWalletService implements NeoWalletService {

    private NeoWalletDao neoWalletDao;

    private User user;

    private PasswordGenerator passwordGenerator;

    private Neow3jClient neow3JClient;

    private UserService userService;

    @Override
    public Pagination<NeoWallet> getWallets(int offset, int count, String userId) {
        return getWalletDao().getWallets(offset, count, userId);
    }

    @Override
    public NeoWallet getWallet(String walletNameOrId) {
        return getWalletDao().getWallet(walletNameOrId);
    }

    @Override
    public NeoWallet updateWallet(String walletId, UpdateNeoWalletRequest walletRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();
        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        }

        var name = Strings.nullToEmpty(walletRequest.getDisplayName()).trim();
        var password = Strings.nullToEmpty(walletRequest.getPassword()).trim();
        var newPassword = Strings.nullToEmpty(walletRequest.getNewPassword()).trim();

        var neoWallet = getWalletDao().getWallet(walletId);
        try {
            var walletFromElements = getNeow3jClient().elementsWalletToNEP6(neoWallet.getWallet());
            var wallet = getNeow3jClient().updateWallet(walletFromElements, name, password, newPassword);
            return getWalletDao().updateWallet(walletId, walletRequest, getNeow3jClient().nep6ToElementsWallet(wallet));
        } catch (CipherException | NEP2InvalidFormat | NEP2InvalidPassphrase | JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }
    }

    @Override
    public NeoWallet createWallet(CreateNeoWalletRequest walletRequest) {
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
                var wallet = getNeow3jClient().createWallet(walletRequest.getDisplayName());
                var elementsWallet = getNeow3jClient().nep6ToElementsWallet(wallet);
                var neoWallet = new NeoWallet();

                neoWallet.setDisplayName(walletRequest.getDisplayName());
                neoWallet.setWallet(elementsWallet);
                neoWallet.setUser(getUserService().getUser(walletRequest.getUserId()));

                return getWalletDao().createWallet(neoWallet);
            } catch (CipherException | JsonProcessingException e) {
                throw new InternalException(e.getMessage());
            }
        } else {
            try {
                var wallet = getNeow3jClient().createWallet(walletRequest.getDisplayName(), pw);
                var elementsWallet = getNeow3jClient().nep6ToElementsWallet(wallet);
                var neoWallet = new NeoWallet();

                neoWallet.setDisplayName(walletRequest.getDisplayName());
                neoWallet.setWallet(elementsWallet);
                neoWallet.setUser(getUserService().getUser(walletRequest.getUserId()));

                return neoWalletDao.createWallet(neoWallet);
            } catch (CipherException | JsonProcessingException e) {
                throw new InternalException(e.getMessage());
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

    public Neow3jClient getNeow3jClient(){return neow3JClient;}

    @Inject
    public void setNeow3jClient(Neow3jClient neow3JClient){this.neow3JClient = neow3JClient;}
}
