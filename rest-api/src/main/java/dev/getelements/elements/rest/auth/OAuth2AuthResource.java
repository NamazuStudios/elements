package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.session.OAuth2SessionRequest;
import dev.getelements.elements.sdk.model.session.SessionCreation;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.isNullOrEmpty;


@Path("auth/oauth2")
public class OAuth2AuthResource {

    private ValidationHelper validationHelper;

    private OAuth2AuthService oAuth2AuthService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a Session using OAuth2",
            description = "Begins a session by accepting a session request with parameters matching the specified OAuth2" +
                    " Scheme. Upon successful validation against the scheme provided in the path, this will return a " +
                    "Session which can be used for authentication. If there is no User associated with the supplied " +
                    "credentials, this will implicitly create a new account and will include that account information " +
                    "in the response. If there is an account, or this method receives an existing session key, this " +
                    "will link to the existing scheme if the account was not previously linked.")
    public SessionCreation createOAuth2Session(final OAuth2SessionRequest oAuth2SessionRequest) {

        getValidationHelper().validateModel(oAuth2SessionRequest);

        if (isNullOrEmpty(oAuth2SessionRequest.getSchemeId())) {
            throw new InvalidDataException("Scheme id is required.");
        }

        if ((oAuth2SessionRequest.getRequestHeaders() == null || oAuth2SessionRequest.getRequestHeaders().isEmpty()) &&
            (oAuth2SessionRequest.getRequestParameters() == null || oAuth2SessionRequest.getRequestParameters().isEmpty())) {
            throw new InvalidDataException("No information to validate against. Please check headers and/or parameters.");
        }

        return getOAuth2AuthService().createSession(oAuth2SessionRequest);

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public OAuth2AuthService getOAuth2AuthService() {
        return oAuth2AuthService;
    }

    @Inject
    public void setOAuth2AuthService(OAuth2AuthService oAuth2AuthService) {
        this.oAuth2AuthService = oAuth2AuthService;
    }

}

