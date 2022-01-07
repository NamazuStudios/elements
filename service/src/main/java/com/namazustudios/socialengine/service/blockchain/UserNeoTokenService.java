package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.neo.CreateNeoTokenRequest;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import com.namazustudios.socialengine.model.blockchain.neo.UpdateNeoTokenRequest;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import java.util.List;

public class UserNeoTokenService implements NeoTokenService {

    private NeoTokenDao neoTokenDao;

    private Neow3jClient neow3JClient;

    private User user;

    @Override
    public Pagination<NeoToken> getTokens(int offset, int count, List<String> tags, String search) {
        return getNeoTokenDao().getTokens(offset, count, tags, search);
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
