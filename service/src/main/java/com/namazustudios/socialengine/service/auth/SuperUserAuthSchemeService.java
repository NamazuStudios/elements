package com.namazustudios.socialengine.service.auth;

import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.service.util.CryptoKeyPairUtility;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;
import java.util.Base64;
import java.util.List;

public class SuperUserAuthSchemeService implements AuthSchemeService {

    private AuthSchemeDao authSchemeDao;

    private CryptoKeyPairUtility cryptoKeyPairUtility;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<AuthScheme> getAuthSchemes(final int offset, final int count, final List<String> tags) {
        return getAuthSchemeDao().getAuthSchemes(offset, count, tags);
    }

    @Override
    public AuthScheme getAuthScheme(final String authSchemeId) {
        return getAuthSchemeDao().getAuthScheme(authSchemeId);
    }

    @Override
    public CreateAuthSchemeResponse createAuthScheme(final CreateAuthSchemeRequest authSchemeRequest) {

        getValidationHelper().validateModel(authSchemeRequest, Create.class);

        final var response = new CreateAuthSchemeResponse();

        final var authScheme = new AuthScheme();
        authScheme.setTags(authSchemeRequest.getTags());
        authScheme.setAlgorithm(authSchemeRequest.getAlgorithm());
        authScheme.setAudience(authSchemeRequest.getAudience());
        authScheme.setUserLevel(authSchemeRequest.getUserLevel());
        authScheme.setAllowedIssuers(authSchemeRequest.getAllowedIssuers());

        if (authSchemeRequest.getPublicKey() == null) {
            final var keyPair = getJwtCryptoUtility().generateKeyPair(authSchemeRequest.getAlgorithm());
            response.setPublicKey(keyPair.getPublicKeyBase64());
            response.setPrivateKey(keyPair.getPrivateKeyBase64());
            authScheme.setPublicKey(keyPair.getPublicKeyBase64());
        } else {

            final var publicKey = getJwtCryptoUtility().getPublicKey(
                    authSchemeRequest.getAlgorithm(),
                    authSchemeRequest.getPublicKey()
            );

            final var encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            response.setPublicKey(encoded);
            authScheme.setPublicKey(encoded);

        }

        final var result = getAuthSchemeDao().createAuthScheme(authScheme);
        response.setScheme(result);

        return response;

    }

    @Override
    public UpdateAuthSchemeResponse updateAuthScheme(final String authSchemeId,
                                                     final UpdateAuthSchemeRequest authSchemeRequest) {

        getValidationHelper().validateModel(authSchemeRequest, Update.class);

        final var response = new UpdateAuthSchemeResponse();
        final var authScheme = getAuthSchemeDao().getAuthScheme(authSchemeId);

        authScheme.setTags(authSchemeRequest.getTags());
        authScheme.setAlgorithm(authSchemeRequest.getAlgorithm());
        authScheme.setAudience(authSchemeRequest.getAudience());
        authScheme.setUserLevel(authSchemeRequest.getUserLevel());
        authScheme.setAllowedIssuers(authSchemeRequest.getAllowedIssuers());

        if (authSchemeRequest.isRegenerate()) {

            if (authSchemeRequest.getPublicKey() != null) {
                throw new BadRequestException("Cannot specify both a public key and regeneration in the same request.");
            }

            final var keyPair = getJwtCryptoUtility().generateKeyPair(authSchemeRequest.getAlgorithm());
            response.setPublicKey(keyPair.getPublicKeyBase64());
            response.setPrivateKey(keyPair.getPrivateKeyBase64());
            authScheme.setPublicKey(keyPair.getPublicKeyBase64());

        } else if (authSchemeRequest.getPublicKey() != null) {

            final var publicKey = getJwtCryptoUtility().getPublicKey(
                authSchemeRequest.getAlgorithm(),
                authSchemeRequest.getPublicKey()
            );

            final var encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            response.setPublicKey(encoded);
            authScheme.setPublicKey(encoded);

        }

        final var authSchemeResult = getAuthSchemeDao().updateAuthScheme(authScheme);
        response.setScheme(authSchemeResult);
        response.setPublicKey(authSchemeResult.getPublicKey());

        return response;

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

    public CryptoKeyPairUtility getJwtCryptoUtility() {
        return cryptoKeyPairUtility;
    }

    @Inject
    public void setJwtCryptoUtility(CryptoKeyPairUtility cryptoKeyPairUtility) {
        this.cryptoKeyPairUtility = cryptoKeyPairUtility;
    }

    public CryptoKeyPairUtility getCryptoKeyUtility() {
        return cryptoKeyPairUtility;
    }

    @Inject
    public void setCryptoKeyUtility(CryptoKeyPairUtility cryptoKeyPairUtility) {
        this.cryptoKeyPairUtility = cryptoKeyPairUtility;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
