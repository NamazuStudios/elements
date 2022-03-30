package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.BscTokenDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscTokenRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscToken;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscTokenRequest;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import java.util.List;

public class SuperUserBscTokenService implements BscTokenService {

    private BscTokenDao bscTokenDao;

    private User user;

    @Override
    public Pagination<BscToken> getTokens(
            final int offset,
            final int count, List<String> tags,
            final BlockchainConstants.MintStatus mintStatus,
            final String search) {
        return getBscTokenDao().getTokens(offset, count, tags, mintStatus, search);
    }

    @Override
    public BscToken getToken(String tokenIdOrName) {
        return getBscTokenDao().getToken(tokenIdOrName);
    }

    @Override
    public BscToken updateToken(String tokenId, UpdateBscTokenRequest tokenRequest) {
        return getBscTokenDao().updateToken(tokenId, tokenRequest);
    }

    @Override
    public BscToken createToken(CreateBscTokenRequest tokenRequest) {
        return getBscTokenDao().createToken(tokenRequest);
    }

    @Override
    public void deleteToken(String tokenId) {
        getBscTokenDao().deleteToken(tokenId);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public BscTokenDao getBscTokenDao() {
        return bscTokenDao;
    }

    @Inject
    public void setBscTokenDao(BscTokenDao bscTokenDao) {
        this.bscTokenDao = bscTokenDao;
    }

}
