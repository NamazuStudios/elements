package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateNeoTokenRequest;
import com.namazustudios.socialengine.model.blockchain.NeoToken;
import com.namazustudios.socialengine.model.blockchain.UpdateNeoTokenRequest;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import java.util.List;

public class SuperUserNeoTokenService implements NeoTokenService {

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
    public NeoToken updateToken(String tokenId, UpdateNeoTokenRequest tokenRequest) {
        return getNeoTokenDao().updateToken(tokenId, tokenRequest);
    }

    @Override
    public NeoToken createToken(CreateNeoTokenRequest tokenRequest) {

        var token = getNeoTokenDao().getToken(tokenRequest.getToken().getName());

        if (token != null) {
            throw new DuplicateException(String.format("NeoToken with name: %s already exists.", tokenRequest.getToken().getName()));
        }

        return getNeoTokenDao().createToken(tokenRequest);
    }

    @Override
    public void deleteToken(String tokenId) {
        getNeoTokenDao().deleteToken(tokenId);
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
