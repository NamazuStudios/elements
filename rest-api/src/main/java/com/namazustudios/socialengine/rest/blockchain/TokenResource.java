package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.blockchain.Token;

import com.namazustudios.socialengine.service.blockchain.TokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by keithhudnall on 9/21/21.
 */
@Api(value = "Neo Smart Contract Templates",
        description = "Allows for the storage and retrieval of compiled Neo smart contracts.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/neo/template")
public class TokenResource {

    private TokenService tokenService;

    @GET
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Neo Smart Contract Template",
            notes = "Gets a specific Neo Smart Contract Template by templateId.")
    public Token getToken(@PathParam("templateId") String tokenId) {

        tokenId = Strings.nullToEmpty(tokenId).trim();

        if (tokenId.isEmpty()) {
            throw new NotFoundException();
        }

        return getTokenService().getToken(tokenId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Neo Smart Contract Template",
            notes = "Creates a new Neo Smart Contract Template, associated with the specified application.")
    public Token createToken(final String templateId) {
        return getTokenService().createToken(templateId);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Neo Smart Contract Template",
            notes = "Updates a Neo Smart Contract Template with the specified name or id.")
    public Token updateToken(final String templateId) {
        return getTokenService().updateToken(templateId);
    }

    @DELETE
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Neo Smart Contract Template",
            notes = "Deletes a Neo Smart Contract Template with the specified name or id.")
    public void deleteToken(@PathParam("templateId") String nameOrId) {

        nameOrId = Strings.nullToEmpty(nameOrId).trim();

        if (nameOrId.isEmpty()) {
            throw new NotFoundException();
        }

        getTokenService().deleteToken(nameOrId);
    }

    public TokenService getTokenService() {
        return tokenService;
    }

    @Inject
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }
}
