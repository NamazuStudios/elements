package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.service.WalletService;
import com.namazustudios.socialengine.service.blockchain.crypto.WalletCryptoUtilities;
import com.namazustudios.socialengine.service.blockchain.crypto.WalletIdentityFactory;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;

import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

public class SuperUserWalletService implements WalletService {

    private UserDao userDao;

    private VaultDao vaultDao;

    private WalletDao walletDao;

    private ValidationHelper validationHelper;

    private WalletCryptoUtilities walletCryptoUtilities;

    private WalletIdentityFactory walletIdentityFactory;

    @Override
    public Pagination<Wallet> getWallets(
            final int offset, final int count,
            final String userId,
            final String vaultId,
            final BlockchainApi protocol,
            final List<BlockchainNetwork> networks) {
        return getWalletDao().getWallets(offset, count, userId, protocol, networks);
    }

    @Override
    public Wallet getWallet(final String walletId) {
        return getWalletDao().getWallet(walletId);
    }

    @Override
    public Wallet getWallet(final String walletId, final String vaultId) {
        return getWalletDao().getWallet(walletId, vaultId);
    }

    @Override
    public Wallet updateWallet(final String vaultId,
                               final String walletId,
                               final UpdateWalletRequest walletUpdateRequest) {

        getValidationHelper().validateModel(walletUpdateRequest);

        var wallet = getWalletDao().getWallet(walletId);

        wallet.getApi().validate(walletUpdateRequest.getNetworks());
        wallet.setNetworks(walletUpdateRequest.getNetworks());
        wallet.setDisplayName(walletUpdateRequest.getDisplayName());

        final var vault = getVaultDao()
                .findVault(vaultId)
                .orElseThrow(() -> new InvalidDataException("No such user."));

        wallet.setVault(vault);

        final var displayName = walletUpdateRequest.getDisplayName();

        if (displayName != null) {
            wallet.setDisplayName(displayName);
        }

//        if (!passphrase.isBlank() && !newPassphrase.isBlank()) {
//            wallet = getWalletCryptoUtilities()
//                    .reEncrypt(wallet, passphrase, newPassphrase)
//                    .orElseThrow(() -> new InvalidDataException("Invalid Passphrase."));
//        } else if (!passphrase.isBlank() || !newPassphrase.isBlank()) {
//            throw new InvalidDataException("Must specify both old and new passphrase.");
//        }

        return getWalletDao().updateWallet(wallet);

    }

    @Override
    public Wallet createWallet(final String vaultId,
                               final CreateWalletRequest createWalletRequest) {

        getValidationHelper().validateModel(createWalletRequest);

        createWalletRequest
                .getApi()
                .validate(createWalletRequest.getNetworks());

        var wallet = new Wallet();
        wallet.setApi(createWalletRequest.getApi());
        wallet.setDisplayName(createWalletRequest.getDisplayName());
        wallet.setApi(createWalletRequest.getApi());
        wallet.setNetworks(createWalletRequest.getNetworks());
        wallet.setAccounts(createWalletRequest.getIdentities());
        wallet.setPreferredAccount(createWalletRequest.getDefaultIdentity());

        final var vault = getVaultDao()
                .findVault(vaultId)
                .orElseThrow(() -> new InvalidDataException("No such user."));

        wallet.setVault(vault);

        final var identities = createWalletRequest.getIdentities();

        if (identities == null || identities.isEmpty()) {
            wallet = getWalletIdentityFactory().create(wallet);
        } else if (wallet.getPreferredAccount() > identities.size()) {
            throw new InvalidDataException("Default must be less than identity collection.");
        }

        final var passphrase = nullToEmpty(createWalletRequest.getPassphrase()).trim();

        if (passphrase.isBlank()) {
            for (var identity : createWalletRequest.getIdentities()) {
                if (!identity.isEncrypted()) {
                    throw new InvalidDataException("Must supply encrypted wallet if not supplying passphrase.");
                }
            }
        } else {

            for (var identity : createWalletRequest.getIdentities()) {
                if (identity.isEncrypted()) {
                    throw new InvalidDataException("Must supply unencrypted wallet if supplying passphrase.");
                }
            }

            wallet.setAccounts(createWalletRequest.getIdentities());
            wallet = getWalletCryptoUtilities().encrypt(wallet, passphrase);

        }

        return getWalletDao().createWallet(wallet);

    }

    @Override
    public void deleteWallet(final String walletId) {
        getWalletDao().deleteWallet(walletId);
    }

    @Override
    public void deleteWallet(final String walletId, final String vaultId) {
        // TODO Honor Vault ID
        getWalletDao().deleteWallet(walletId);
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

    public WalletDao getWalletDao() {
        return walletDao;
    }

    @Inject
    public void setWalletDao(WalletDao walletDao) {
        this.walletDao = walletDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public WalletCryptoUtilities getWalletCryptoUtilities() {
        return walletCryptoUtilities;
    }

    @Inject
    public void setWalletCryptoUtilities(WalletCryptoUtilities walletCryptoUtilities) {
        this.walletCryptoUtilities = walletCryptoUtilities;
    }

    public WalletIdentityFactory getWalletIdentityFactory() {
        return walletIdentityFactory;
    }

    @Inject
    public void setWalletIdentityFactory(WalletIdentityFactory walletIdentityFactory) {
        this.walletIdentityFactory = walletIdentityFactory;
    }

}
