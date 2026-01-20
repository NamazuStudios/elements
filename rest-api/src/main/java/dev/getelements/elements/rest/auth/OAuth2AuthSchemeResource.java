package dev.getelements.elements.rest.auth;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOAuth2AuthSchemeRequest;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateOAuth2AuthSchemeResponse;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.service.auth.OAuth2AuthSchemeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("auth_scheme/oauth2")
public class OAuth2AuthSchemeResource {

    private OAuth2AuthSchemeService oAuth2AuthSchemeService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Lists all auth schemes in the system",
            description = "Requires SUPERUSER access. Gets a pagination of Auth Schemes for the given query.")
    public Pagination<OAuth2AuthScheme> getOauth2AuthSchemes(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") final List<String> tags) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getOAuth2AuthSchemeService().getAuthSchemes(offset, count, tags);
    }

    @GET
    @Path("{oAuth2AuthSchemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Gets a specific Auth Scheme",
            description = "Gets a specific Auth Scheme by the oAuth2AuthSchemeId.")
    public OAuth2AuthScheme getOauth2AuthScheme(@PathParam("oAuth2AuthSchemeId") String oAuth2AuthSchemeId) {

        oAuth2AuthSchemeId = Strings.nullToEmpty(oAuth2AuthSchemeId).trim();

        if (oAuth2AuthSchemeId.isEmpty()) {
            throw new NotFoundException();
        }

        return getOAuth2AuthSchemeService().getAuthScheme(oAuth2AuthSchemeId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Creates a new Auth Scheme",
            description = "Creates a new Auth Scheme, from the data in the given auth scheme request")
    public CreateOrUpdateOAuth2AuthSchemeResponse createOauth2AuthScheme(final CreateOrUpdateOAuth2AuthSchemeRequest oAuth2AuthSchemeRequest) {
        return getOAuth2AuthSchemeService().createAuthScheme(oAuth2AuthSchemeRequest);
    }

    @PUT
    @Path("{oAuth2AuthSchemeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Updates an Auth Scheme",
            description = "Updates an Auth Scheme with the specified data in the auth scheme request.")
    public CreateOrUpdateOAuth2AuthSchemeResponse updateOauth2AuthScheme(
            @PathParam("oAuth2AuthSchemeId")
            final String oAuth2AuthSchemeId,
            final CreateOrUpdateOAuth2AuthSchemeRequest oAuth2AuthSchemeRequest) {
        return getOAuth2AuthSchemeService().updateAuthScheme(oAuth2AuthSchemeId, oAuth2AuthSchemeRequest);
    }

    @DELETE
    @Path("{oAuth2AuthSchemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Deletes an Auth Scheme",
            description = "Deletes an Auth Scheme with the specified id.")
    public void deleteOauth2AuthScheme(@PathParam("oAuth2AuthSchemeId") String oAuth2AuthSchemeId) {

        oAuth2AuthSchemeId = Strings.nullToEmpty(oAuth2AuthSchemeId).trim();

        if (oAuth2AuthSchemeId.isEmpty()) {
            throw new NotFoundException();
        }

        getOAuth2AuthSchemeService().deleteAuthScheme(oAuth2AuthSchemeId);
    }

    public OAuth2AuthSchemeService getOAuth2AuthSchemeService() {
        return oAuth2AuthSchemeService;
    }

    @Inject
    public void setOAuth2AuthSchemeService(OAuth2AuthSchemeService oAuth2AuthSchemeService) {
        this.oAuth2AuthSchemeService = oAuth2AuthSchemeService;
    }
}
