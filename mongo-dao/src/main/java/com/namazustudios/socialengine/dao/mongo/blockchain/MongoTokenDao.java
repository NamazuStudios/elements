package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.socialengine.dao.TokenDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateTokenRequest;
import com.namazustudios.socialengine.model.blockchain.Token;

public class MongoTokenDao implements TokenDao {

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
}
