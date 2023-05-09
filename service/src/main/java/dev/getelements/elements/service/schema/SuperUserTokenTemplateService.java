package dev.getelements.elements.service.schema;

import dev.getelements.elements.dao.TokenTemplateDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.template.CreateTokenTemplateRequest;
import dev.getelements.elements.model.schema.template.TokenTemplate;
import dev.getelements.elements.model.schema.template.UpdateTokenTemplateRequest;
import dev.getelements.elements.model.user.User;

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
