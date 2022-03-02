package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.neo.CreateNeoWalletRequest;
import com.namazustudios.socialengine.model.blockchain.neo.NeoWallet;
import com.namazustudios.socialengine.model.blockchain.neo.UpdateNeoWalletRequest;
import com.namazustudios.socialengine.service.blockchain.NeoWalletService;
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
@Api(value = "Neo Wallets",
        description = "Allows for the storage and retrieval of compiled Neo wallets.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/neo/wallet")
public class NeoWalletResource {

    private NeoWalletService neoWalletService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Neo wallets. Optionally filtered for a specific user",
            notes = "Gets a pagination of Neo Wallets. Optionally a user Id can be specified to filter for a given user.")
    public Pagination<NeoWallet> getWallets(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("userId") String userId,
            @QueryParam("format") @DefaultValue("NONE") String format) {

        return getWalletService().getWallets(offset, count, userId);
    }
    
    @GET
    @Path("{walletNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Neo Wallet",
            notes = "Gets a specific Neo Wallet by Id.")
    public NeoWallet getWallet(@PathParam("walletNameOrId") String walletNameOrId) {

        walletNameOrId = Strings.nullToEmpty(walletNameOrId).trim();

        return getWalletService().getWallet(walletNameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Neo Wallet",
            notes = "Creates a new Neo Wallet, associated with the given user.")
    public NeoWallet createWallet(final CreateNeoWalletRequest request) {
        return getWalletService().createWallet(request);
    }

    @PUT
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Neo Wallet",
            notes = "Updates a Neo Wallet with the specified name or id.")
    public NeoWallet updateWallet(@PathParam("walletId") String walletId, final UpdateNeoWalletRequest request) {
        return getWalletService().updateWallet(walletId, request);
    }

    @DELETE
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Neo Wallet",
            notes = "Deletes a Neo Wallet with the specified id.")
    public void deleteTemplate(@PathParam("walletId") String walletId) {
        getWalletService().deleteWallet(walletId);
    }


    @GET
    @Path("{walletNameOrId}/nft")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets the NFT contents",
            notes = "Gets the NFT (NEP-11) contents of a specific Neo Wallet.")
    public List<Token> getWalletNFTContents(@PathParam("walletNameOrId") String walletNameOrId) {

        walletNameOrId = Strings.nullToEmpty(walletNameOrId).trim();

        return getWalletService().getWalletNFTContents(walletNameOrId);
    }

    public NeoWalletService getWalletService() {
        return neoWalletService;
    }

    @Inject
    public void setWalletService(NeoWalletService neoWalletService) {
        this.neoWalletService = neoWalletService;
    }
}
