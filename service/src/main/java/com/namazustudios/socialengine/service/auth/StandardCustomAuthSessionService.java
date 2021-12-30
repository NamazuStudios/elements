package com.namazustudios.socialengine.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.dao.CustomAuthUserDao;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.security.JWTCredentials;
import com.namazustudios.socialengine.service.CustomAuthSessionService;

import javax.inject.Inject;

public class StandardCustomAuthSessionService implements CustomAuthSessionService {

    private ObjectMapper objectMapper;

    private AuthSchemeDao authSchemeDao;

    private CustomAuthUserDao customAuthUserDao;

    @Override
    public Session getSession(final String jwt) {
        final var decoded = new JWTCredentials(jwt);
        getAuthSchemeDao().getAuthSchemesByAudience(decoded.getAudience());

        return null;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public AuthSchemeDao getAuthSchemeDao() {
        return authSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(AuthSchemeDao authSchemeDao) {
        this.authSchemeDao = authSchemeDao;
    }

    public CustomAuthUserDao getCustomAuthUserDao() {
        return customAuthUserDao;
    }

    @Inject
    public void setCustomAuthUserDao(CustomAuthUserDao customAuthUserDao) {
        this.customAuthUserDao = customAuthUserDao;
    }

}
