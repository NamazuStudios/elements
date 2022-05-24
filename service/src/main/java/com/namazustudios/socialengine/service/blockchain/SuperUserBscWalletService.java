package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.service.UserService;
import org.web3j.crypto.CipherException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SuperUserBscWalletService implements BscWalletService {

    private BscWalletDao BscWalletDao;

    private User user;

    private PasswordGenerator passwordGenerator;

    private Bscw3jClient bscw3JClient;

    private UserService userService;

    private ObjectMapper objectMapper;

    @Override
    public Pagination<BscWallet> getWallets(final int offset, final int count, final String userId) {
        return getWalletDao().getWallets(offset, count, userId);
    }

    @Override
    public BscWallet getWallet(final String walletNameOrId) {
        return getWalletDao().getWallet(walletNameOrId);
    }

    @Override
    public BscWallet updateWallet(final String walletId, final UpdateBscWalletRequest walletRequest) {
        final var user = getUser();
        final var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();

        if (userId.isEmpty()) {
            walletRequest.setUserId(user.getId());
        }

        final var name = Strings.nullToEmpty(walletRequest.getDisplayName()).trim();
        final var password = Strings.nullToEmpty(walletRequest.getPassword()).trim();
        final var newPassword = Strings.nullToEmpty(walletRequest.getNewPassword()).trim();

        final var BscWallet = getWalletDao().getWallet(walletId);
        try {
            final var wallet = getBscw3jClient().updateWallet(BscWallet.getWallet(), name, password, newPassword);
            return getWalletDao().updateWallet(walletId, walletRequest, wallet);
        } catch (final CipherException | JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }
    }

    @Override
    public BscWallet createWallet(final CreateBscWalletRequest walletRequest) {

        final var user = getUser();
        final var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();

        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        }

        final var existing = getWalletDao().getWalletForUser(walletRequest.getUserId(), walletRequest.getDisplayName());

        if (existing != null) {
            throw new DuplicateException(String.format("Wallet with name: %s already exists.", walletRequest.getDisplayName()));
        }

        final var pw = Strings.nullToEmpty(walletRequest.getPassword()).trim();
        final var wif = Strings.nullToEmpty(walletRequest.getPrivateKey()).trim();

        try {
            final var wallet = wif.isEmpty() ?
                    getBscw3jClient().createWallet(walletRequest.getDisplayName(), pw) :
                    getBscw3jClient().createWallet(walletRequest.getDisplayName(), pw, wif);

            final var BscWallet = new BscWallet();

            BscWallet.setDisplayName(walletRequest.getDisplayName());
            BscWallet.setWallet(wallet);
            BscWallet.setUser(getUserService().getUser(walletRequest.getUserId()));

            return getWalletDao().createWallet(BscWallet);
        } catch (final CipherException | JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }
    }


    @Override
    public void deleteWallet(final String walletId) {
        getWalletDao().deleteWallet(walletId);
    }

    public BscWalletDao getWalletDao() {
        return BscWalletDao;
    }

    @Inject
    public void setWalletDao(final BscWalletDao BscWalletDao) {
        this.BscWalletDao = BscWalletDao;
    }

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(final UserService userService) {
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
    public void setPasswordGenerator(final PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Bscw3jClient getBscw3jClient(){return bscw3JClient;}

    @Inject
    public void setBscw3jClient(final Bscw3jClient bscw3JClient){this.bscw3JClient = bscw3JClient;}

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
