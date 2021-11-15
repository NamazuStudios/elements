package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.TokenDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateTokenRequest;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.UpdateTokenRequest;

import javax.inject.Inject;
import java.util.List;

public class UserTokenService implements TokenService {

    private TokenDao tokenDao;

    private Neow3jClient neow3JClient;

    @Override
    public Pagination<Token> getTokens(int offset, int count, List<String> tags, String search) {
        throw new NotImplementedException();
//        return getTokenDao().getTokens(offset, count, tags, search);
    }

    @Override
    public Token getToken(String tokenIdOrName) {
        throw new NotImplementedException();
//        return getTokenDao().getToken(tokenIdOrName);
    }

    @Override
    public Token updateToken(UpdateTokenRequest updateTokenRequest) {
        throw new NotImplementedException();
//        return getTokenDao().updateToken(updateTokenRequest);
    }

    @Override
    public Token createToken(CreateTokenRequest tokenRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteToken(String tokenId) {
        throw new NotImplementedException();
//        getTokenDao().deleteToken(tokenId);
    }

    public TokenDao getTokenDao() {
        return tokenDao;
    }

    @Inject
    public void setTokenDao(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public Neow3jClient getNeow3jClient(){return neow3JClient;}

    @Inject
    public void setNeow3jClient(Neow3jClient neow3JClient){this.neow3JClient = neow3JClient;}
}
