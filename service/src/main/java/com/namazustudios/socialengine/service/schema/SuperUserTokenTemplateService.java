package com.namazustudios.socialengine.service.schema;

import com.namazustudios.socialengine.dao.TokenTemplateDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.schema.template.CreateTokenTemplateRequest;
import com.namazustudios.socialengine.model.schema.template.TokenTemplate;
import com.namazustudios.socialengine.model.schema.template.UpdateTokenTemplateRequest;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;

public class SuperUserTokenTemplateService implements TokenTemplateService {

    private TokenTemplateDao metadataSpecDao;

    private User user;

    @Override
    public Pagination<TokenTemplate> getTokenTemplates(
            final int offset,
            final int count) {
        return getTokenTemplateDao().getTokenTemplates(offset, count, null);
    }

    @Override
    public TokenTemplate getTokenTemplate(String metadataSpecIdOrName) {
        return getTokenTemplateDao().getTokenTemplate(metadataSpecIdOrName, null);
    }

    @Override
    public TokenTemplate updateTokenTemplate(String metadataSpecId, UpdateTokenTemplateRequest metadataSpecRequest) {
        return getTokenTemplateDao().updateTokenTemplate(metadataSpecId, metadataSpecRequest);
    }

    @Override
    public TokenTemplate createTokenTemplate(CreateTokenTemplateRequest metadataSpecRequest) {
        return getTokenTemplateDao().createTokenTemplate(metadataSpecRequest);
    }

    @Override
    public void deleteTokenTemplate(String metadataSpecId) {
        getTokenTemplateDao().deleteTokenTemplate(metadataSpecId);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public TokenTemplateDao getTokenTemplateDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setTokenTemplateDao(TokenTemplateDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }
}
