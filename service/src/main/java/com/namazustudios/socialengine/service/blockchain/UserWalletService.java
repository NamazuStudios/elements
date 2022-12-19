package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.WalletService;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

public class UserWalletService implements WalletService {

    private User user;

    private WalletDao walletDao;

    private SuperUserWalletService superUserWalletService;

    @Override
    public Pagination<Wallet> getWallets(
            final int offset, final int count,
            final String userId, final BlockchainApi protocol, final List<BlockchainNetwork> networks) {
        if (userId == null || Objects.equals(userId, getUser().getId())) {
            return getWalletDao().getWallets(offset, count, getUser().getId(), protocol, networks);
        } else {
            return Pagination.empty();
        }
    }

    @Override
    public Wallet getWallet(final String walletNameOrId) {
        return getWalletDao().getWallet(walletNameOrId, getUser().getId());
    }

    @Override
    public Wallet updateWallet(final String walletId, final UpdateWalletRequest walletUpdateRequest) {

        var userId = walletUpdateRequest.getUserId();

        if (userId == null) {
            userId = getUser().getId();
        } else if (!Objects.equals(userId, getUser().getId())) {
            throw new InvalidDataException("Invalid user id: " + userId);
        }

        walletUpdateRequest.setUserId(userId);
        return getSuperUserWalletService().updateWallet(walletId, walletUpdateRequest);

    }

    @Override
    public Wallet createWallet(final CreateWalletRequest createWalletRequest) {

        var userId = createWalletRequest.getUserId();

        if (userId == null) {
            userId = getUser().getId();
        } else if (!Objects.equals(userId, getUser().getId())) {
            throw new InvalidDataException("Invalid user id: " + userId);
        }

        createWalletRequest.setUserId(userId);
        return getSuperUserWalletService().createWallet(createWalletRequest);

    }

    @Override
    public void deleteWallet(final String walletId) {
        getWalletDao().deleteWallet(walletId, getUser().getId());
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public WalletDao getWalletDao() {
        return walletDao;
    }

    @Inject
    public void setWalletDao(WalletDao walletDao) {
        this.walletDao = walletDao;
    }

    public SuperUserWalletService getSuperUserWalletService() {
        return superUserWalletService;
    }

    @Inject
    public void setSuperUserWalletService(SuperUserWalletService superUserWalletService) {
        this.superUserWalletService = superUserWalletService;
    }

}
