package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.NeoTokenDao;
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
        throw new NotImplementedException();
//        return getTokenDao().getTokens(offset, count, tags, search);
    }

    @Override
    public NeoToken getToken(String tokenIdOrName) {
        throw new NotImplementedException();
//        return getTokenDao().getToken(tokenIdOrName);
    }

    @Override
    public NeoToken updateToken(UpdateNeoTokenRequest tokenRequest) {
        throw new NotImplementedException();
//        return getTokenDao().updateToken(tokenRequest);
    }

    @Override
    public NeoToken createToken(CreateNeoTokenRequest tokenRequest) {
        throw new NotImplementedException();
//        return getTokenDao().createToken(tokenRequest);
    }

    @Override
    public void deleteToken(String templateId) {
        throw new NotImplementedException();
//        getTokenDao().deleteToken(templateId);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public NeoTokenDao getTokenDao() {
        return neoTokenDao;
    }

    @Inject
    public void setTokenDao(NeoTokenDao neoTokenDao) {
        this.neoTokenDao = neoTokenDao;
    }

    public Neow3jClient getNeow3jClient(){return neow3JClient;}

    @Inject
    public void setNeow3jClient(Neow3jClient neow3JClient){this.neow3JClient = neow3JClient;}
}
