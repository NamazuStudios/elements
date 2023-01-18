package com.namazustudios.socialengine.service.blockchain.omni;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.service.VaultService;
import com.namazustudios.socialengine.service.blockchain.crypto.VaultCryptoUtilities;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;

public class SuperUserVaultService implements VaultService {

    private UserDao userDao;

    private VaultDao vaultDao;

    private ValidationHelper validationHelper;

    private VaultCryptoUtilities vaultCryptoUtilities;

    @Override
    public Pagination<Vault> getVaults(final int offset, final int count, final String userId) {
        return getVaultDao().getVaults(offset, count, userId);
    }

    @Override
    public Vault getVault(final String vaultId) {
        return getVaultDao().getVault(vaultId);
    }

    @Override
    public Vault createVault(final CreateVaultRequest createVaultRequest) {

        getValidationHelper().validateModel(createVaultRequest);

        final var vault = new Vault();

        final var user = getUserDao()
                .findActiveUser(createVaultRequest.getUserId())
                .orElseThrow(InvalidDataException::new);

        vault.setUser(user);
        vault.setDisplayName(createVaultRequest.getDisplayName());

        final var algorithm = createVaultRequest.getAlgorithm();
        final var passphrase = nullToEmpty(createVaultRequest.getPassphrase()).trim();

        final var key = passphrase.isBlank()
                ? getVaultCryptoUtilities().generateKey(algorithm)
                : getVaultCryptoUtilities().generateKey(algorithm, passphrase);

        vault.setKey(key);

        return getVaultDao().createVault(vault);

    }

    @Override
    public Vault updateVault(final String vaultId, final UpdateVaultRequest updateVaultRequest) {

        getValidationHelper().validateModel(updateVaultRequest);

        final var vault = getVault(vaultId);

        final var user = getUserDao()
                .findActiveUser(updateVaultRequest.getUserId())
                .orElseThrow(InvalidDataException::new);

        vault.setUser(user);
        vault.setDisplayName(updateVaultRequest.getDisplayName());

        final var passphrase = nullToEmpty(updateVaultRequest.getPassphrase()).trim();
        final var newPassphrase = nullToEmpty(updateVaultRequest.getNewPassphrase()).trim();

        final var existingKey = vault.getKey();

        if (existingKey.isEncrypted()) {

            if (!passphrase.isBlank() && !newPassphrase.isBlank()) {

                final var key = getVaultCryptoUtilities().reEncryptKey(
                        vault.getKey(),
                        passphrase,
                        newPassphrase
                ).orElseThrow(() -> new InvalidDataException("Incorrect passphrase."));

                vault.setKey(key);

            } else if (passphrase.isBlank() != newPassphrase.isBlank()) {
                throw new InvalidDataException("Must specify both old and new passphrase, if specified.");
            }

        } else if (!newPassphrase.isBlank()) {
            final var key = getVaultCryptoUtilities().encryptKey(existingKey, newPassphrase);
            vault.setKey(key);
        }

        return getVaultDao().updateVault(vault);

    }

    @Override
    public void deleteVault(final String vaultId) {
        getVaultDao().deleteVault(vaultId);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public VaultDao getVaultDao() {
        return vaultDao;
    }

    @Inject
    public void setVaultDao(VaultDao vaultDao) {
        this.vaultDao = vaultDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public VaultCryptoUtilities getVaultCryptoUtilities() {
        return vaultCryptoUtilities;
    }

    @Inject
    public void setVaultCryptoUtilities(VaultCryptoUtilities vaultCryptoUtilities) {
        this.vaultCryptoUtilities = vaultCryptoUtilities;
    }

}
