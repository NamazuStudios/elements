package dev.getelements.elements.service.blockchain.neo;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.dao.NeoTokenDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.neo.CreateNeoTokenRequest;
import dev.getelements.elements.model.blockchain.neo.NeoToken;
import dev.getelements.elements.model.blockchain.neo.UpdateNeoTokenRequest;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.blockchain.neo.NeoTokenService;
import dev.getelements.elements.service.blockchain.neo.Neow3jClient;

import javax.inject.Inject;
import java.util.List;

public class UserNeoTokenService implements NeoTokenService {

    private NeoTokenDao neoTokenDao;

    private Neow3jClient neow3JClient;

    private User user;

    @Override
    public Pagination<NeoToken> getTokens(
            final int offset,
            final int count,
            final List<String> tags,
            final List<BlockchainConstants.MintStatus> mintStatus,
            final String search) {
        return getNeoTokenDao().getTokens(offset, count, tags, mintStatus, search);
    }

    @Override
    public NeoToken getToken(String tokenIdOrName) {
        return getNeoTokenDao().getToken(tokenIdOrName);
    }

    @Override
    public NeoToken updateToken(String tokenId, UpdateNeoTokenRequest updateNeoTokenRequest) {
        throw new ForbiddenException("You do not have sufficient permissions to perform this action");
    }

    @Override
    public NeoToken createToken(CreateNeoTokenRequest tokenRequest) {
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

    public NeoTokenDao getNeoTokenDao() {
        return neoTokenDao;
    }

    @Inject
    public void setNeoTokenDao(NeoTokenDao neoTokenDao) {
        this.neoTokenDao = neoTokenDao;
    }

    public Neow3jClient getNeow3jClient(){return neow3JClient;}

    @Inject
    public void setNeow3jClient(Neow3jClient neow3JClient){this.neow3JClient = neow3JClient;}
}
