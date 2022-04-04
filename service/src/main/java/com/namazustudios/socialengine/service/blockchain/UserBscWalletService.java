package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.security.InsufficientPermissionException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.PasswordGenerator;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.wallet.Wallet;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        return getWalletDao().getWallet(walletId);
    }

    @Override
    public BscWallet updateWallet(String walletId, UpdateBscWalletRequest walletRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();
        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        } else if(!user.getId().equals(userId)){
            throw new InsufficientPermissionException("You do not have permission to update a wallet for another user.");
        }

        var name = Strings.nullToEmpty(walletRequest.getDisplayName()).trim();
        var password = Strings.nullToEmpty(walletRequest.getPassword()).trim();
        var newPassword = Strings.nullToEmpty(walletRequest.getNewPassword()).trim();

        var neoWallet = getWalletDao().getWallet(walletId);
        try {
            var walletFromElements = getBscw3jClient().elementsWalletToNEP6(neoWallet.getWallet());
            var wallet = getBscw3jClient().updateWallet(walletFromElements, name, password, newPassword);
            return getWalletDao().updateWallet(walletId, walletRequest, getBscw3jClient().nep6ToElementsWallet(wallet));
        } catch (CipherException | NEP2InvalidFormat | NEP2InvalidPassphrase | JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }
    }

    @Override
    public BscWallet createWallet(CreateBscWalletRequest walletRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(walletRequest.getUserId()).trim();
        if (userId.isEmpty()){
            walletRequest.setUserId(user.getId());
        } else if(!user.getId().equals(userId)){
            throw new InsufficientPermissionException("You do not have permission to create a wallet for another user.");
        }
        var pw = Strings.nullToEmpty(walletRequest.getPassword()).trim();

        var existing = getWalletDao().getWalletForUser(walletRequest.getUserId(), walletRequest.getDisplayName());
        if (existing != null) {
            throw new DuplicateException(String.format("Wallet with name: %s already exists.", walletRequest.getDisplayName()));
        }

        if (pw.isEmpty()){
            try {
                var wallet = getBscw3jClient().createWallet(walletRequest.getDisplayName());
                var elementsWallet = getBscw3jClient().nep6ToElementsWallet(wallet);
                var bscWallet = new BscWallet();

                bscWallet.setDisplayName(walletRequest.getDisplayName());
                bscWallet.setWallet(elementsWallet);
                bscWallet.setUser(user);

                return bscWalletDao.createWallet(bscWallet);
            } catch (CipherException | JsonProcessingException e) {
                throw new InternalException(e.getMessage());
            }
        } else {
            try {
                var wallet = getBscw3jClient().createWallet(walletRequest.getDisplayName(), pw);
                var elementsWallet = getBscw3jClient().nep6ToElementsWallet(wallet);
                var bscWallet = new BscWallet();

                bscWallet.setDisplayName(walletRequest.getDisplayName());
                bscWallet.setWallet(elementsWallet);
                bscWallet.setUser(user);

                return bscWalletDao.createWallet(bscWallet);
            } catch (CipherException | JsonProcessingException e) {
                throw new InternalException(e.getMessage());
            }
        }
    }

    @Override
    public List<Token> getWalletNFTContents(final String walletNameOrId) {

        final var wallet = getWalletDao().getWallet(walletNameOrId);
        final var nepWallet = getBscw3jClient().elementsWalletToNEP6(wallet.getWallet());
        final var mintAccount = Wallet.fromNEP6Wallet(nepWallet).getDefaultAccount();

        final var client = getBscw3jClient().getNeow3j();

        final var getNep11BalanacesRequest = client.getNep11Balances(mintAccount.getScriptHash());

        try {
            final var rawRequest = getNep11BalanacesRequest.send();
            final var response = rawRequest.getBalances();
            final var balances = response.getBalances();

            var first = balances.get(0);
            var ft = first.getTokens().get(0);
            var id = ft.getTokenId();

            for (final var balance : balances) {

                for (final var token : balance.getTokens()) {

                    final var tokenProperties = client.getNep11Properties(balance.getAssetHash(), token.getTokenId());

                    final var result = tokenProperties.sendAsync().get();

                    final var properties = result.getProperties();
                }
            }

        } catch (IOException e) {
            throw new InternalException(e.getMessage());
        } catch (ExecutionException e) {
            throw new InternalException(e.getMessage());
        } catch (InterruptedException e) {
            throw new InternalException(e.getMessage());
        }

        return null;
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
