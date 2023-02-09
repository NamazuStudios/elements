package com.namazustudios.socialengine.service.blockchain.bsc;

import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.exception.security.InsufficientPermissionException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.service.blockchain.bsc.BscWalletService;
import com.namazustudios.socialengine.service.blockchain.bsc.Bscw3jClient;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;

public class UserBscWalletService implements BscWalletService {

    private BscWalletDao bscWalletDao;

    private User user;

    private PasswordGenerator passwordGenerator;

    private Bscw3jClient bscw3JClient;

    @Override
    public Pagination<BscWallet> getWallets(int offset, int count, String search) {
        return getWalletDao().getWallets(offset, count, search);
    }

    @Override
    public BscWallet getWallet(String walletId) {
        return getWalletDao().getWalletForUser(user.getId(), walletId);
    }

    @Override
    public BscWallet updateWallet(String walletId, UpdateBscWalletRequest walletRequest) {

        final var user = getUser();
        final var userId = nullToEmpty(walletRequest.getUserId()).trim();

        if (userId.isEmpty()) {
            walletRequest.setUserId(user.getId());
        } else if(!user.getId().equals(userId)){
            throw new InsufficientPermissionException();
        }

        final var name = nullToEmpty(walletRequest.getDisplayName()).trim();
        final var password = nullToEmpty(walletRequest.getPassword()).trim();
        final var displayName = nullToEmpty(walletRequest.getDisplayName());
        final var newPassword = nullToEmpty(walletRequest.getNewPassword()).trim();

        final var bscWallet = getWalletDao().getWallet(walletId);
        final var web3jWallet = getBscw3jClient().updateWallet(bscWallet.getWallet(), name, password, newPassword);

        bscWallet.setUser(user);
        bscWallet.setWallet(web3jWallet);
        bscWallet.setDisplayName(displayName);

        return getWalletDao().updateWallet(bscWallet);

    }

    @Override
    public BscWallet createWallet(final CreateBscWalletRequest createBscWalletRequest) {

        var user = getUser();
        var userId = nullToEmpty(createBscWalletRequest.getUserId()).trim();

        if (userId.isEmpty()) {
            createBscWalletRequest.setUserId(user.getId());
        } else if(!user.getId().equals(userId)) {
            throw new InsufficientPermissionException();
        }

        final var pw = nullToEmpty(createBscWalletRequest.getPassword()).trim();
        final var pk = nullToEmpty(createBscWalletRequest.getPrivateKey()).trim();

        final var wallet = pk.isEmpty() ?
            getBscw3jClient().createWallet(createBscWalletRequest.getDisplayName(), pw) :
            getBscw3jClient().createWallet(createBscWalletRequest.getDisplayName(), pw, pk);

        var bscWallet = new BscWallet();
        bscWallet.setDisplayName(createBscWalletRequest.getDisplayName());
        bscWallet.setWallet(wallet);
        bscWallet.setUser(user);

        return bscWalletDao.createWallet(bscWallet);

    }

    @Override
    public void deleteWallet(String walletId) {
        getWalletDao().deleteWallet(walletId);
    }

    public BscWalletDao getWalletDao() {
        return bscWalletDao;
    }

    @Inject
    public void setWalletDao(BscWalletDao bscWalletDao) {
        this.bscWalletDao = bscWalletDao;
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
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Bscw3jClient getBscw3jClient(){return bscw3JClient;}

    @Inject
    public void setBscw3jClient(Bscw3jClient bscw3JClient){this.bscw3JClient = bscw3JClient;}
}
