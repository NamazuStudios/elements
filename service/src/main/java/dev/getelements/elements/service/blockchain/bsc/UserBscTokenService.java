package dev.getelements.elements.service.blockchain.bsc;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.dao.BscTokenDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.bsc.CreateBscTokenRequest;
import dev.getelements.elements.model.blockchain.bsc.BscToken;
import dev.getelements.elements.model.blockchain.bsc.UpdateBscTokenRequest;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.blockchain.bsc.BscTokenService;
import dev.getelements.elements.service.blockchain.bsc.Bscw3jClient;

import javax.inject.Inject;
import java.util.List;

public class UserBscTokenService implements BscTokenService {

    private BscTokenDao bscTokenDao;

    private Bscw3jClient bscw3JClient;

    private User user;

    @Override
    public Pagination<BscToken> getTokens(
            final int offset,
            final int count,
            final List<String> tags,
            final List<BlockchainConstants.MintStatus> mintStatus,
            final String search) {
        return getBscTokenDao().getTokens(offset, count, tags, mintStatus, search);
    }

    @Override
    public BscToken getToken(String tokenIdOrName) {
        return getBscTokenDao().getToken(tokenIdOrName);
    }

    @Override
    public BscToken updateToken(String tokenId, UpdateBscTokenRequest updateBscTokenRequest) {
        throw new ForbiddenException("You do not have sufficient permissions to perform this action");
    }

    @Override
    public BscToken createToken(CreateBscTokenRequest tokenRequest) {
        throw new ForbiddenException("You do not have sufficient permissions to perform this action");
    }

    @Override
    public void deleteToken(String tokenId) {
        throw new ForbiddenException("You do not have sufficient permissions to perform this action");
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

    public Bscw3jClient getBscw3jClient(){return bscw3JClient;}

    @Inject
    public void setBscw3jClient(Bscw3jClient bscw3JClient){this.bscw3JClient = bscw3JClient;}
}
