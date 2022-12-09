package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainProtocol;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.service.WalletService;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;

public class SuperUserWalletService implements WalletService {

    private UserDao userDao;

    private WalletDao walletDao;

    private ValidationHelper validationHelper;

    private WalletCryptoUtilities walletCryptoUtilities;

    private WalletIdentityFactory walletIdentityFactory;

    @Override
    public Pagination<Wallet> getWallets(
            final int offset, final int count,
            final String userId, final BlockchainProtocol protocol, final BlockchainNetwork network) {
        return getWalletDao().getWallets(offset, count, userId, protocol, network);
    }

    @Override
    public Wallet getWallet(final String walletNameOrId) {
        return getWalletDao().getWallet(walletNameOrId);
    }

    @Override
    public Wallet updateWallet(final String walletId, final UpdateWalletRequest walletRequest) {

        getValidationHelper().validateModel(walletRequest);

        var wallet = getWalletDao().getWallet(walletId);
        validate(wallet.getProtocol(), walletRequest.getNetworks());

        final var userId = walletRequest.getUserId();

        if (userId != null) {
            final var user = getUserDao()
                    .findActiveUser(userId)
                    .orElseThrow(() -> new BadRequestException("No such user."));
            wallet.setUser(user);
        }

        final var displayName = walletRequest.getDisplayName();

        if (displayName != null) {
            wallet.setDisplayName(displayName);
        }

        final var passphrase = nullToEmpty(walletRequest.getPassphrase()).trim();
        final var newPassphrase = nullToEmpty(walletRequest.getNewPassphrase()).trim();

        if (!passphrase.isBlank() && !newPassphrase.isBlank()) {
            wallet = getWalletCryptoUtilities()
                    .reEncrypt(wallet, passphrase, newPassphrase)
                    .orElseThrow(() -> new BadRequestException("Invalid Passphrase."));
        }

        return getWalletDao().updateWallet(wallet);

    }

    @Override
    public Wallet createWallet(final CreateWalletRequest walletRequest) {

        getValidationHelper().validateModel(walletRequest);

        var wallet = new Wallet();
        wallet.setProtocol(walletRequest.getProtocol());
        wallet.setDisplayName(walletRequest.getDisplayName());
        validate(walletRequest.getProtocol(), walletRequest.getNetworks());

        final var userId = nullToEmpty(walletRequest.getUserId()).trim();

        if (userId.isBlank()) {
            throw new BadRequestException("Invalid user id: " + userId);
        }

        final var user = getUserDao()
                .findActiveUser(userId)
                .orElseThrow(() -> new BadRequestException("No such user: " + userId));

        wallet.setUser(user);

        wallet = getWalletIdentityFactory().create(wallet);

        final var passphrase = nullToEmpty(walletRequest.getPassphrase()).trim();

        if (!passphrase.isBlank()) {
            wallet = getWalletCryptoUtilities().encrypt(wallet, passphrase);
        }

        return getWalletDao().createWallet(wallet);

    }

    private void validate(final BlockchainProtocol protocol, final List<BlockchainNetwork> networks) {
        for (var network : networks) {

            if (network == null) {
                final var msg = format("Network must not be null.");
                throw new BadRequestException(msg);
            }

            if (!Objects.equals(protocol, network.protocol())) {
                final var msg = format("Network %s does not match protocol %s", network, protocol);
                throw new BadRequestException(msg);
            }

        }
    }

    @Override
    public void deleteWallet(final String walletId) {
        getWalletDao().deleteWallet(walletId);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
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
