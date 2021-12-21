package com.namazustudios.socialengine.rest.auth;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.service.auth.AuthSchemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "Auth Schemes",
        description = "An Auth Scheme consists of a public key and the metadata associated with it to tell Elements " +
                "what it needs to verify a JWT key. ",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("auth_scheme")
public class AuthSchemeResource {

    private AuthSchemeService authSchemeService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all auth schemes in the system",
            notes = "Requires SUPERUSER access. Gets a pagination of Auth Schemes for the given query.")
    public Pagination<AuthScheme> getAuthSchemes(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") @DefaultValue("") final List<String> tags) {

        return getAuthSchemeService().getAuthSchemes(offset, count, tags);
    }

    @GET
    @Path("{authSchemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Auth Scheme",
            notes = "Gets a specific Auth Scheme by the authSchemeId.")
    public AuthScheme getAuthScheme(@PathParam("authSchemeId") String authSchemeId) {

        return getAuthSchemeService().getAuthScheme(authSchemeId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Auth Scheme",
            notes = "Creates a new Auth Scheme, from the data in the given auth scheme request")
    public CreateAuthSchemeResponse createAuthScheme(final CreateAuthSchemeRequest authSchemeRequest) {

        return getAuthSchemeService().createAuthScheme(authSchemeRequest);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates an Auth Scheme",
            notes = "Updates an Auth Scheme with the specified data in the auth scheme request.")
    public UpdateAuthSchemeResponse updateAuthScheme(final UpdateAuthSchemeRequest authSchemeRequest) {
        return getAuthSchemeService().updateAuthScheme(authSchemeRequest);
    }

    @DELETE
    @Path("{authSchemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes an Auth Scheme",
            notes = "Deletes an Auth Scheme with the specified id.")
    public void deleteAuthScheme(@PathParam("authSchemeId") String authSchemeId) {

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
