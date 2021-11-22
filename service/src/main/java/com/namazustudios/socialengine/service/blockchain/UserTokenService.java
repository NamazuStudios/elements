package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateTokenRequest;
import com.namazustudios.socialengine.model.blockchain.NeoToken;
import com.namazustudios.socialengine.model.blockchain.UpdateTokenRequest;

import javax.inject.Inject;
import java.util.List;

public class UserTokenService implements TokenService {

    private NeoTokenDao neoTokenDao;

    private Neow3jClient neow3JClient;

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
    public NeoToken updateToken(UpdateTokenRequest updateTokenRequest) {
        throw new NotImplementedException();
//        return getTokenDao().updateToken(updateTokenRequest);
    }

    @Override
    public NeoToken createToken(CreateTokenRequest tokenRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteToken(String tokenId) {
        throw new NotImplementedException();
//        getTokenDao().deleteToken(tokenId);
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
