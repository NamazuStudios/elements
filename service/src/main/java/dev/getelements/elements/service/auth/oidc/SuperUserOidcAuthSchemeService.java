package dev.getelements.elements.service.auth.oidc;

import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.auth.*;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OidcAuthSchemeService;
import jakarta.inject.Inject;

import java.util.List;

public class SuperUserOidcAuthSchemeService implements OidcAuthSchemeService {


    private OidcAuthSchemeDao authSchemeDao;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<OidcAuthScheme> getAuthSchemes(final int offset, final int count, final List<String> tags) {
        return getAuthSchemeDao().getAuthSchemes(offset, count, tags);
    }

    @Override
    public OidcAuthScheme getAuthScheme(final String authSchemeId) {
        return getAuthSchemeDao().getAuthScheme(authSchemeId);
    }

    @Override
    public CreateOrUpdateOidcAuthSchemeResponse createAuthScheme(final CreateOrUpdateOidcAuthSchemeRequest authSchemeRequest) {

        getValidationHelper().validateModel(authSchemeRequest, ValidationGroups.Create.class);

        final var authScheme = new OidcAuthScheme();
        authScheme.setKeys(authSchemeRequest.getKeys());
        authScheme.setIssuer(authSchemeRequest.getIssuer());
        authScheme.setKeysUrl(authSchemeRequest.getKeysUrl());
        authScheme.setMediaType(authSchemeRequest.getMediaType());

        final var result = getAuthSchemeDao().createAuthScheme(authScheme);
        final var response = new CreateOrUpdateOidcAuthSchemeResponse();

        response.setScheme(result);

        return response;

    }

    @Override
    public CreateOrUpdateOidcAuthSchemeResponse updateAuthScheme(final String authSchemeId,
                                                                 final CreateOrUpdateOidcAuthSchemeRequest authSchemeRequest) {

        getValidationHelper().validateModel(authSchemeRequest, ValidationGroups.Update.class);

        final var authScheme = getAuthSchemeDao().getAuthScheme(authSchemeId);
        authScheme.setKeys(authSchemeRequest.getKeys());
        authScheme.setIssuer(authSchemeRequest.getIssuer());
        authScheme.setKeysUrl(authSchemeRequest.getKeysUrl());
        authScheme.setMediaType(authSchemeRequest.getMediaType());

        final var result = getAuthSchemeDao().updateAuthScheme(authScheme);
        final var response = new CreateOrUpdateOidcAuthSchemeResponse();

        response.setScheme(result);

        return response;
    }

    @Override
    public void deleteAuthScheme(String authSchemeId) {
        getAuthSchemeDao().deleteAuthScheme(authSchemeId);
    }

    public OidcAuthSchemeDao getAuthSchemeDao() {
        return authSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(OidcAuthSchemeDao authSchemeDao) {
        this.authSchemeDao = authSchemeDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
