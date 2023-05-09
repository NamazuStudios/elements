package dev.getelements.elements.service.blockchain.bsc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import dev.getelements.elements.dao.BscWalletDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.bsc.BscWallet;
import dev.getelements.elements.model.blockchain.bsc.CreateBscWalletRequest;
import dev.getelements.elements.model.blockchain.bsc.UpdateBscWalletRequest;
import dev.getelements.elements.service.blockchain.bsc.BscWalletService;
import dev.getelements.elements.service.blockchain.bsc.Bscw3jClient;

import javax.inject.Inject;

public class SuperUserBscWalletService implements BscWalletService {

    private UserDao userDao;

    private BscWalletDao BscWalletDao;

    private Bscw3jClient bscw3JClient;

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
    public BscWallet updateWallet(final String walletId, final UpdateBscWalletRequest updateBscWalletRequest) {

        final var userId = Strings.nullToEmpty(updateBscWalletRequest.getUserId()).trim();
        final var userOptional = getUserDao().findActiveUser(userId);

        final var displayName = Strings.nullToEmpty(updateBscWalletRequest.getDisplayName()).trim();
        final var password = Strings.nullToEmpty(updateBscWalletRequest.getPassword()).trim();
        final var newPassword = Strings.nullToEmpty(updateBscWalletRequest.getNewPassword()).trim();

        if (userOptional.isEmpty()) {
            throw new InvalidDataException("User not found: " + userId);
        }

        final var bscWallet = getWalletDao().getWallet(walletId);
        bscWallet.setUser(userOptional.get());
        bscWallet.setDisplayName(displayName);

        final var web3jWallet = getBscw3jClient().updateWallet(bscWallet.getWallet(),
            displayName,
            password,
            newPassword
        );

        bscWallet.setWallet(web3jWallet);
        return getWalletDao().updateWallet(bscWallet);

    }

    @Override
    public BscWallet createWallet(final CreateBscWalletRequest createBscWalletRequest) {

        final var userId = Strings.nullToEmpty(createBscWalletRequest.getUserId()).trim();
        final var userOptional = getUserDao().findActiveUser(userId);

        if (userOptional.isEmpty()) {
            throw new InvalidDataException("User not found: " + userId);
        }

        final var pw = Strings.nullToEmpty(createBscWalletRequest.getPassword()).trim();
        final var pk = Strings.nullToEmpty(createBscWalletRequest.getPrivateKey()).trim();

        final var wallet = pk.isEmpty() ?
            getBscw3jClient().createWallet(createBscWalletRequest.getDisplayName(), pw) :
            getBscw3jClient().createWallet(createBscWalletRequest.getDisplayName(), pw, pk);

        final var bscWallet = new BscWallet();
        bscWallet.setDisplayName(createBscWalletRequest.getDisplayName());
        bscWallet.setWallet(wallet);
        bscWallet.setUser(userOptional.get());

        return getWalletDao().createWallet(bscWallet);

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

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
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
