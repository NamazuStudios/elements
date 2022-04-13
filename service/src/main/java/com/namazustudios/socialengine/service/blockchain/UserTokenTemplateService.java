package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.TokenTemplateDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.template.CreateTokenTemplateRequest;
import com.namazustudios.socialengine.model.blockchain.template.TokenTemplate;
import com.namazustudios.socialengine.model.blockchain.template.UpdateTokenTemplateRequest;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;

public class UserTokenTemplateService implements TokenTemplateService {

    private TokenTemplateDao tokenTemplateDao;

    private User user;

    @Override
    public Pagination<TokenTemplate> getTokens(
            final int offset,
            final int count) {
        return getTokenTemplateDao().getTokenTemplates(offset, count);
    }

    @Override
    public TokenTemplate getTokenTemplate(String templateId) {
        return getTokenTemplateDao().getTokenTemplate(templateId);
    }

    @Override
    public TokenTemplate updateTokenTemplate(String templateId, UpdateTokenTemplateRequest tokenRequest) {
        return getTokenTemplateDao().updateTokenTemplate(templateId, tokenRequest);
    }

    @Override
    public TokenTemplate createTokenTemplate(CreateTokenTemplateRequest tokenRequest) {
        return getTokenTemplateDao().createTokenTemplate(tokenRequest);
    }

    @Override
    public void deleteTokenTemplate(String templateId) {
        getTokenTemplateDao().deleteTokenTemplate(templateId);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public TokenTemplateDao getTokenTemplateDao() {
        return tokenTemplateDao;
    }

    @Inject
    public void setTokenTemplateDao(TokenTemplateDao tokenTemplateDao) {
        this.tokenTemplateDao = tokenTemplateDao;
    }
}
