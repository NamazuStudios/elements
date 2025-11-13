package dev.getelements.elements.service.auth;


import dev.getelements.elements.sdk.dao.OAuth2AuthSchemeDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOAuth2AuthSchemeRequest;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOAuth2AuthSchemeResponse;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthSchemeService;
import jakarta.inject.Inject;

import java.util.List;

public class SuperUserOAuth2AuthSchemeService implements OAuth2AuthSchemeService {

    private OAuth2AuthSchemeDao authSchemeDao;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<OAuth2AuthScheme> getAuthSchemes(final int offset, final int count, final List<String> tags) {
        return getAuthSchemeDao().getAuthSchemes(offset, count, tags);
    }

    @Override
    public OAuth2AuthScheme getAuthScheme(final String authSchemeId) {
        return getAuthSchemeDao().getAuthScheme(authSchemeId);
    }

    @Override
    public CreateOrUpdateOAuth2AuthSchemeResponse createAuthScheme(final CreateOrUpdateOAuth2AuthSchemeRequest authSchemeRequest) {

        getValidationHelper().validateModel(authSchemeRequest, ValidationGroups.Create.class);

        final var authScheme = new OAuth2AuthScheme();

        authScheme.setName(authSchemeRequest.getName());
        authScheme.setHeaders(authSchemeRequest.getHeaders());
        authScheme.setParams(authSchemeRequest.getParams());
        authScheme.setValidationUrl(authSchemeRequest.getValidationUrl());
        authScheme.setResponseIdMapping(authSchemeRequest.getResponseIdMapping());


        final var result = getAuthSchemeDao().createAuthScheme(authScheme);
        final var response = new CreateOrUpdateOAuth2AuthSchemeResponse();

        response.setScheme(result);

        return response;

    }

    @Override
    public CreateOrUpdateOAuth2AuthSchemeResponse updateAuthScheme(final String authSchemeId,
                                                                 final CreateOrUpdateOAuth2AuthSchemeRequest authSchemeRequest) {

        getValidationHelper().validateModel(authSchemeRequest, ValidationGroups.Update.class);

        final var authScheme = getAuthSchemeDao().getAuthScheme(authSchemeId);

        authScheme.setHeaders(authSchemeRequest.getHeaders());
        authScheme.setParams(authSchemeRequest.getParams());
        authScheme.setValidationUrl(authSchemeRequest.getValidationUrl());
        authScheme.setResponseIdMapping(authSchemeRequest.getResponseIdMapping());

        final var authSchemeResult = getAuthSchemeDao().updateAuthScheme(authScheme);
        final var response = new CreateOrUpdateOAuth2AuthSchemeResponse();

        response.setScheme(authSchemeResult);

        return response;
    }

    @Override
    public void deleteAuthScheme(String authSchemeId) {
        getAuthSchemeDao().deleteAuthScheme(authSchemeId);
    }

    public OAuth2AuthSchemeDao getAuthSchemeDao() {
        return authSchemeDao;
    }

    @Inject
    public void setAuthSchemeDao(OAuth2AuthSchemeDao authSchemeDao) {
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
