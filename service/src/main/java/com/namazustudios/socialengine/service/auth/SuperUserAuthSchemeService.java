package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.rt.exception.BadRequestException;

import javax.inject.Inject;
import java.util.List;

public class SuperUserAuthSchemeService implements AuthSchemeService {

    private AuthSchemeDao authSchemeDao;

    @Override
    public Pagination<AuthScheme> getAuthSchemes(int offset, int count, List<String> tags) {
        return getAuthSchemeDao().getAuthSchemes(offset, count, tags);
    }

    @Override
    public AuthScheme getAuthScheme(String authSchemeId) {
        return getAuthSchemeDao().getAuthScheme(authSchemeId);
    }

    @Override
    public UpdateAuthSchemeResponse updateAuthScheme(UpdateAuthSchemeRequest authSchemeRequest) {
        if (authSchemeRequest.getRegenerate()) {
            if (authSchemeRequest.getPubKey() != null)
            {
                throw new BadRequestException();
            }
            // TODO regenerate pub/private key pair
        }
        return getAuthSchemeDao().updateAuthScheme(authSchemeRequest);
    }

    @Override
    public CreateAuthSchemeResponse createAuthScheme(CreateAuthSchemeRequest authSchemeRequest) {
        // generate public/private key
        if (authSchemeRequest.getPubKey() == null) {
            //TODO generate pub/private key pair
        }
        return getAuthSchemeDao().createAuthScheme(authSchemeRequest);
    }

    @Override
    public void deleteAuthScheme(String authSchemeId) {
        getAuthSchemeDao().deleteAuthScheme(authSchemeId);
    }

    public AuthSchemeDao getAuthSchemeDao() {
        return authSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(AuthSchemeDao authSchemeDao) {
        this.authSchemeDao = authSchemeDao;
    }
}
