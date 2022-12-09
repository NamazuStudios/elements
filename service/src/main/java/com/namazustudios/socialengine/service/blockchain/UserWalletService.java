package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainProtocol;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.service.WalletService;

import javax.inject.Inject;
import java.util.Objects;

public class UserWalletService implements WalletService {

    private User user;

    private WalletDao walletDao;

    private SuperUserWalletService superUserWalletService;

    @Override
    public Pagination<Wallet> getWallets(
            final int offset, final int count,
            final String userId, final BlockchainProtocol protocol, final BlockchainNetwork network) {
        return null;
    }

    @Override
    public Wallet getWallet(final String walletNameOrId) {
        return getWalletDao().getWallet(walletNameOrId, user.getId());
    }

    @Override
    public Wallet updateWallet(final String walletId, final UpdateWalletRequest walletRequest) {

        var userId = walletRequest.getUserId();

        if (userId == null) {
            userId = getUser().getId();
        } else if (!Objects.equals(userId, getUser().getId())) {
            throw new BadRequestException("Invalid user id: " + userId);
        }

        walletRequest.setUserId(userId);
        return getSuperUserWalletService().updateWallet(walletId, walletRequest);

    }

    @Override
    public Wallet createWallet(final CreateWalletRequest walletRequest) {

        var userId = walletRequest.getUserId();

        if (userId == null) {
            userId = getUser().getId();
        } else if (!Objects.equals(userId, getUser().getId())) {
            throw new BadRequestException("Invalid user id: " + userId);
        }

        walletRequest.setUserId(userId);
        return getSuperUserWalletService().createWallet(walletRequest);

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
