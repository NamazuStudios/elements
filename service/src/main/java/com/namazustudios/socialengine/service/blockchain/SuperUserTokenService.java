package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.TokenDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateTokenRequest;
import com.namazustudios.socialengine.model.blockchain.Token;

import javax.inject.Inject;

public class SuperUserTokenService implements TokenService {

    private TokenDao tokenDao;

    @Override
    public Pagination<Token> getTokens(int offset, int count, String search) {
        return null;
    }

    @Override
    public Token getToken(String tokenIdOrName) {
        return null;
    }

    @Override
    public Token updateToken(String tokenId) {
        return null;
    }

    @Override
    public Token createToken(CreateTokenRequest tokenRequest) {
        return null;
    }

    @Override
    public void deleteToken(String templateId) {

    }

    public TokenDao getTokenDao() {
        return tokenDao;
    }

    @Inject
    public void setTokenDao(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }
}
