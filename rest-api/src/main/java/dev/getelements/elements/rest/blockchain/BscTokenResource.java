package dev.getelements.elements.rest.blockchain;

import com.google.common.base.Strings;
import dev.getelements.elements.BlockchainConstants.MintStatus;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.bsc.CreateBscTokenRequest;
import dev.getelements.elements.model.blockchain.bsc.BscToken;
import dev.getelements.elements.model.blockchain.bsc.UpdateBscTokenRequest;
import dev.getelements.elements.service.blockchain.bsc.BscTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by TuanTran on 3/24/24.
 */
@Api(value = "Bsc Tokens",
        description = "Allows for the storage and retrieval of compiled Bsc tokens.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/bsc/token")
public class BscTokenResource {

    private BscTokenService bscTokenService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Bsc Tokens",
            notes = "Gets a pagination of Bsc Tokens for the given query.")
    public Pagination<BscToken> getTokens(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") @DefaultValue("") final List<String> tags,
            @QueryParam("mintStatus") final List<MintStatus> mintStatus,
            @QueryParam("query") String query) {
        return getTokenService().getTokens(offset, count, tags, mintStatus, query);
    }

    @GET
    @Path("{tokenNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Bsc BscToken",
            notes = "Gets a specific BscToken by token name or Id.")
    public BscToken getToken(@PathParam("tokenNameOrId") String tokenNameOrId) {
        return getTokenService().getToken(tokenNameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new BscToken definition",
            notes = "Creates a new BscToken definition.")
    public BscToken createToken(final CreateBscTokenRequest tokenRequest) {
        return getTokenService().createToken(tokenRequest);
    }

    @PUT
    @Path("{tokenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a BscToken",
            notes = "Updates a BscToken with the specified id.")
    public BscToken updateToken(@PathParam("tokenId") String tokenId, final UpdateBscTokenRequest tokenRequest) {
        return getTokenService().updateToken(tokenId, tokenRequest);
    }

    @DELETE
    @Path("{tokenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a BscToken",
            notes = "Deletes a BscToken with the specified id.")
    public void deleteToken(@PathParam("tokenId") String tokenId) {

        tokenId = Strings.nullToEmpty(tokenId).trim();

        if (tokenId.isEmpty()) {
            throw new NotFoundException();
        }

        getTokenService().deleteToken(tokenId);
    }

    public BscTokenService getTokenService() {
        return bscTokenService;
    }

    @Inject
    public void setTokenService(BscTokenService bscTokenService) {
        this.bscTokenService = bscTokenService;
    }

}
