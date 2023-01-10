package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
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

    private VaultDao vaultDao;

    private WalletDao walletDao;

    private SuperUserWalletService superUserWalletService;

    @Override
    public Pagination<Wallet> getWallets(
            final int offset, final int count,
            final String userId, final String vaultId,
            final BlockchainApi protocol, final List<BlockchainNetwork> networks) {
        if (userId == null || Objects.equals(userId, getUser().getId())) {
            return getWalletDao().getWallets(offset, count, getUser().getId(), protocol, networks);
        } else {
            return Pagination.empty();
        }
    }

    @Override
    public Wallet getWallet(final String walletId) {
        return getWalletDao().getWallet(walletId, getUser().getId());
    }

    @Override
    public Wallet getWallet(final String walletId, final String vaultId) {
        final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
        return getWalletDao().getWallet(walletId, vault.getId());
    }

    @Override
    public Wallet updateWallet(final String vaultId,
                               final String walletId,
                               final UpdateWalletRequest walletUpdateRequest) {
        final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
        return getSuperUserWalletService().updateWallet(vault.getId(), walletId, walletUpdateRequest);
    }

    @Override
    public Wallet createWallet(final String vaultId, final CreateWalletRequest createWalletRequest) {
        final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
        return getSuperUserWalletService().createWallet(vault.getId(), createWalletRequest);
    }

    @Override
    public void deleteWallet(final String walletId) {
        getWalletDao().deleteWalletForUser(walletId, getUser().getId());
    }

    @Override
    public void deleteWallet(final String walletId, final String vaultId) {
        final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
        getWalletDao().deleteWalletForUser(walletId, vault.getId());
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public VaultDao getVaultDao() {
        return vaultDao;
    }

    @Inject
    public void setVaultDao(VaultDao vaultDao) {
        this.vaultDao = vaultDao;
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
