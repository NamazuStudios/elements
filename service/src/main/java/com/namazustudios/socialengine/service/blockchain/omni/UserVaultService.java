package com.namazustudios.socialengine.service.blockchain.omni;

import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.VaultService;

import javax.inject.Inject;

public class UserVaultService implements VaultService {

    private User user;

    private VaultDao vaultDao;

    private SuperUserVaultService superUserVaultService;

    @Override
    public Pagination<Vault> getVaults(final int offset, final int count, final String userId) {
        if (userId == null || userId.equals(getUser().getId())) {
            return getVaultDao().getVaults(offset, count, getUser().getId());
        } else {
            return Pagination.empty();
        }
    }

    @Override
    public Vault getVault(final String vaultId) {
        return getVaultDao().getVaultForUser(vaultId, getUser().getId());
    }

    @Override
    public Vault createVault(final CreateVaultRequest request) {

        if (request.getUserId() == null) {
            request.setUserId(getUser().getId());
        } else if (!request.getUserId().equals(getUser().getId())) {
            throw new InvalidDataException("Cannot create for another user.");
        }

        return getSuperUserVaultService().createVault(request);

    }

    @Override
    public Vault updateVault(final String vaultId, final UpdateVaultRequest request) {

        if (request.getUserId() == null) {
            request.setUserId(getUser().getId());
        } else if (!request.getUserId().equals(getUser().getId())) {
            throw new InvalidDataException("Cannot modify vault user.");
        }

        return getSuperUserVaultService().updateVault(vaultId, request);

    }

    @Override
    public void deleteVault(final String vaultId) {
        getVaultDao().deleteVaultForUser(vaultId, getUser().getId());
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

    public SuperUserVaultService getSuperUserVaultService() {
        return superUserVaultService;
    }

    @Inject
    public void setSuperUserVaultService(SuperUserVaultService superUserVaultService) {
        this.superUserVaultService = superUserVaultService;
    }

}
