package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateNeoTokenRequest;
import com.namazustudios.socialengine.model.blockchain.NeoToken;

import com.namazustudios.socialengine.model.blockchain.UpdateNeoTokenRequest;
import com.namazustudios.socialengine.service.blockchain.NeoTokenService;
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
public class NeoTokenResource {

    private NeoTokenService neoTokenService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Neo Tokens",
            notes = "Gets a pagination of Neo Tokens for the given query.")
    public Pagination<NeoToken> getTokens(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") @DefaultValue("") final List<String> tags,
            @QueryParam("query") String query) {

        return getTokenService().getTokens(offset, count, tags, query);
    }

    @GET
    @Path("{tokenNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Neo NeoToken",
            notes = "Gets a specific NeoToken by token name or Id.")
    public NeoToken getToken(@PathParam("tokenNameOrId") String tokenNameOrId) {

        return getTokenService().getToken(tokenNameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new NeoToken definition",
            notes = "Creates a new NeoToken definition.")
    public NeoToken createToken(final CreateNeoTokenRequest tokenRequest) {
        return getTokenService().createToken(tokenRequest);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a NeoToken",
            notes = "Updates a NeoToken with the specified name or id.")
    public NeoToken updateToken(final UpdateNeoTokenRequest tokenRequest) {
        return getTokenService().updateToken(tokenRequest);
    }

    @DELETE
    @Path("{tokenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a NeoToken",
            notes = "Deletes a NeoToken with the specified id.")
    public void deleteToken(@PathParam("tokenId") String tokenId) {

        tokenId = Strings.nullToEmpty(tokenId).trim();

        if (tokenId.isEmpty()) {
            throw new NotFoundException();
        }

        getTokenService().deleteToken(tokenId);
    }

    public NeoTokenService getTokenService() {
        return neoTokenService;
    }

    @Inject
    public void setTokenService(NeoTokenService neoTokenService) {
        this.neoTokenService = neoTokenService;
    }
}
