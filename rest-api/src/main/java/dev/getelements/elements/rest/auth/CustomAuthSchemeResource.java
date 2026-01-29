package dev.getelements.elements.rest.auth;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.*;

import dev.getelements.elements.sdk.service.auth.AuthSchemeService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("auth_scheme/custom")
public class CustomAuthSchemeResource {

    private AuthSchemeService authSchemeService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Lists all auth schemes in the system",
            description = "Requires SUPERUSER access. Gets a pagination of Auth Schemes for the given query.")
    public Pagination<AuthScheme> getCustomAuthSchemes(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") final List<String> tags) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getAuthSchemeService().getAuthSchemes(offset, count, tags);
    }

    @GET
    @Path("{authSchemeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Gets a specific Auth Scheme",
            description = "Gets a specific Auth Scheme by the authSchemeId.")
    public AuthScheme getCustomAuthScheme(@PathParam("authSchemeId") String authSchemeId) {

        authSchemeId = Strings.nullToEmpty(authSchemeId).trim();

        if (authSchemeId.isEmpty()) {
            throw new NotFoundException();
        }

        return getAuthSchemeService().getAuthScheme(authSchemeId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Creates a new Auth Scheme",
            description = "Creates a new Auth Scheme, from the data in the given auth scheme request")
    public CreateAuthSchemeResponse createCustomAuthScheme(final CreateAuthSchemeRequest authSchemeRequest) {
        return getAuthSchemeService().createAuthScheme(authSchemeRequest);
    }

    @PUT
    @Path("{authSchemeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Updates an Auth Scheme",
            description = "Updates an Auth Scheme with the specified data in the auth scheme request.")
    public UpdateAuthSchemeResponse updateCustomAuthScheme(
            @PathParam("authSchemeId")
            final String authSchemeId,
            final UpdateAuthSchemeRequest authSchemeRequest) {
        return getAuthSchemeService().updateAuthScheme(authSchemeId, authSchemeRequest);
    }

    @DELETE
    @Path("{authSchemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Deletes an Auth Scheme",
            description = "Deletes an Auth Scheme with the specified id.")
    public void deleteCustomAuthScheme(@PathParam("authSchemeId") String authSchemeId) {

        authSchemeId = Strings.nullToEmpty(authSchemeId).trim();

        if (authSchemeId.isEmpty()) {
            throw new NotFoundException();
        }

        getAuthSchemeService().deleteAuthScheme(authSchemeId);
    }

    public AuthSchemeService getAuthSchemeService() {
        return authSchemeService;
    }

    @Inject
    public void setAuthSchemeService(AuthSchemeService authSchemeService) {
        this.authSchemeService = authSchemeService;
    }

}
