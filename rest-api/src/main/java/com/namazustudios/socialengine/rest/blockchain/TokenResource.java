package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateTokenRequest;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import com.namazustudios.socialengine.model.blockchain.Token;

import com.namazustudios.socialengine.model.blockchain.UpdateTokenRequest;
import com.namazustudios.socialengine.service.blockchain.TokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by keithhudnall on 9/21/21.
 */
@Api(value = "Neo Tokens",
        description = "Allows for the storage and retrieval of compiled Neo tokens.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/neo/token")
public class TokenResource {

    private TokenService tokenService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a Neo Tokens for a specific user",
            notes = "Gets a pagination of Neo Tokens for the given query.")
    public Pagination<Token> getTokens(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") @DefaultValue("") final List<String> tags,
            @QueryParam("query") String query) {

        query = Strings.nullToEmpty(query).trim();

        if (query.isEmpty()) {
            throw new NotFoundException();
        }

        return getTokenService().getTokens(offset, count, tags, query);
    }

    @GET
    @Path("{tokenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Neo Token",
            notes = "Gets a specific Neo Token by tokenId.")
    public Token getToken(@PathParam("tokenId") String tokenId) {

        tokenId = Strings.nullToEmpty(tokenId).trim();

        if (tokenId.isEmpty()) {
            throw new NotFoundException();
        }

        return getTokenService().getToken(tokenId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Neo Token",
            notes = "Creates a new Neo Token, associated with the specified smart contract.")
    public Token createToken(final CreateTokenRequest tokenRequest) {
        return getTokenService().createToken(tokenRequest);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Neo Token",
            notes = "Updates a Neo Token with the specified name or id.")
    public Token updateToken(final UpdateTokenRequest tokenRequest) {
        return getTokenService().updateToken(tokenRequest);
    }

    @DELETE
    @Path("{tokenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Neo Token",
            notes = "Deletes a Neo Token with the specified id.")
    public void deleteToken(@PathParam("tokenId") String tokenId) {

        tokenId = Strings.nullToEmpty(tokenId).trim();

        if (tokenId.isEmpty()) {
            throw new NotFoundException();
        }

        getTokenService().deleteToken(tokenId);
    }

    public TokenService getTokenService() {
        return tokenService;
    }

    @Inject
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }
}
