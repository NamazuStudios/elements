package dev.getelements.elements.service.schema;

import com.google.common.base.Strings;
import dev.getelements.elements.dao.TokenTemplateDao;
import dev.getelements.elements.exception.security.InsufficientPermissionException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.template.CreateTokenTemplateRequest;
import dev.getelements.elements.model.schema.template.TokenTemplate;
import dev.getelements.elements.model.schema.template.UpdateTokenTemplateRequest;
import dev.getelements.elements.model.user.User;

import javax.inject.Inject;

public class UserTokenTemplateService implements TokenTemplateService {

    private TokenTemplateDao metadataSpecDao;

    private User user;

    @Override
    public Pagination<TokenTemplate> getTokenTemplates(
            final int offset,
            final int count) {
        return getTokenTemplateDao().getTokenTemplates(offset, count, user.getId());
    }

    @Override
    public TokenTemplate getTokenTemplate(String metadataSpecIdOrName) {
        return getTokenTemplateDao().getTokenTemplate(metadataSpecIdOrName, user.getId());
    }

    @Override
    public TokenTemplate updateTokenTemplate(String metadataSpecId, UpdateTokenTemplateRequest metadataSpecRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(metadataSpecRequest.getUserId()).trim();
        if (userId.isEmpty()){
            metadataSpecRequest.setUserId(user.getId());
        } else if(!user.getId().equals(userId)){
            throw new InsufficientPermissionException("You do not have permission to update a token template for another user.");
        }
        return getTokenTemplateDao().updateTokenTemplate(metadataSpecId, metadataSpecRequest);
    }

    @Override
    public TokenTemplate createTokenTemplate(CreateTokenTemplateRequest metadataSpecRequest) {
        var user = getUser();
        var userId = Strings.nullToEmpty(metadataSpecRequest.getUserId()).trim();
        if (userId.isEmpty()){
            metadataSpecRequest.setUserId(user.getId());
        } else if(!user.getId().equals(userId)){
            throw new InsufficientPermissionException("You do not have permission to create a token template for another user.");
        }
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
