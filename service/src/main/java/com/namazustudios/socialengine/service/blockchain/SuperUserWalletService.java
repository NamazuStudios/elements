package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.*;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.service.WalletService;
import com.namazustudios.socialengine.service.blockchain.crypto.WalletCryptoUtilities;
import com.namazustudios.socialengine.service.blockchain.crypto.WalletIdentityFactory;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;

import java.util.List;

import static java.util.stream.Collectors.toList;

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
            final String vaultId,
            final String userId,
            final BlockchainApi protocol,
            final List<BlockchainNetwork> networks) {
        return getWalletDao().getWallets(offset, count, vaultId, userId, protocol, networks);
    }

    @Override
    public Wallet getWallet(final String walletId) {
        return getWalletDao().getWallet(walletId);
    }

    @Override
    public Wallet getWalletForVault(final String walletId, final String vaultId) {
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
                .orElseThrow(() -> new InvalidDataException("No such vault."));

        wallet.setVault(vault);

        final var displayName = walletUpdateRequest.getDisplayName();

        if (displayName != null) {
            wallet.setDisplayName(displayName);
        }

        return getWalletDao().updateWallet(wallet);

    }

    @Override
    public Wallet createWallet(final String vaultId,
                               final CreateWalletRequest createWalletRequest) {

        getValidationHelper().validateModel(createWalletRequest);

        createWalletRequest
                .getApi()
                .validate(createWalletRequest.getNetworks());

        final var vault = getVaultDao()
                .findVault(vaultId)
                .orElseThrow(() -> new InvalidDataException("No such user."));

        final var accounts =  createWalletRequest
                .getAccounts()
                .stream()
                .map(cra -> convertAccount(createWalletRequest.getApi(), cra))
                .collect(toList());

        var wallet = new Wallet();
        wallet.setAccounts(accounts);
        wallet.setApi(createWalletRequest.getApi());
        wallet.setDisplayName(createWalletRequest.getDisplayName());
        wallet.setApi(createWalletRequest.getApi());
        wallet.setNetworks(createWalletRequest.getNetworks());
        wallet.setPreferredAccount(createWalletRequest.getPreferredAccount());
        wallet.setVault(vault);
        wallet.setUser(vault.getUser());

        final var encrypted = getWalletCryptoUtilities().encrypt(wallet);
        return getWalletDao().createWallet(encrypted);

    }

    private WalletAccount convertAccount(final BlockchainApi api,
                                         final CreateWalletRequestAccount createWalletRequestAccount) {
        if (createWalletRequestAccount == null) {
            return getWalletIdentityFactory().getGenerator(api).generate();
        } else if (createWalletRequestAccount.isGenerate()) {
            getValidationHelper().validateModel(createWalletRequestAccount, CreateWalletRequestAccount.Generate.class);
            return getWalletIdentityFactory().getGenerator(api).generate();
        } else {
            getValidationHelper().validateModel(createWalletRequestAccount, CreateWalletRequestAccount.Import.class);
            final var account = new WalletAccount();
            account.setEncrypted(false);
            account.setAddress(createWalletRequestAccount.getAddress());
            account.setPrivateKey(createWalletRequestAccount.getPrivateKey());
            return account;
        }
    }

    @Override
    public void deleteWallet(final String walletId) {
        getWalletDao().deleteWallet(walletId);
    }

    @Override
    public void deleteWalletFromVault(final String walletId, final String vaultId) {
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
