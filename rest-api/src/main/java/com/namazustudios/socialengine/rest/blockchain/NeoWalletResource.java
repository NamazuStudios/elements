package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.NeoWallet;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.service.blockchain.NeoWalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.Optional;

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
    @ApiOperation(value = "Gets a Neo wallets for a specific user",
            notes = "Gets a pagination of Neo Wallets for the given user id.")
    public Pagination<NeoWallet> getWallets(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("userId") String userId,
            @QueryParam("format") @DefaultValue("NONE") String format) {

        userId = Strings.nullToEmpty(userId).trim();

        if (userId.isEmpty()) {
            throw new NotFoundException();
        }

        return getWalletService().getWallets(offset, count, userId);
    }
    
    @GET
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Neo Wallet",
            notes = "Gets a specific Neo Wallet by templateId.")
    public Optional<NeoWallet> getWallet(@PathParam("walletId") String walletId) {

        walletId = Strings.nullToEmpty(walletId).trim();

        if (walletId.isEmpty()) {
            throw new NotFoundException();
        }

        return getWalletService().getWallet(walletId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Neo Wallet",
            notes = "Creates a new Neo Wallet, associated with the given user.")
    public NeoWallet createWallet(final CreateWalletRequest request) {
        return getWalletService().createWallet(request);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Neo Wallet",
            notes = "Updates a Neo Wallet with the specified name or id.")
    public NeoWallet updateWallet(final UpdateWalletRequest request) {
        return getWalletService().updateWallet(request);
    }

    @DELETE
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Neo Wallet",
            notes = "Deletes a Neo Wallet with the specified name or id.")
    public void deleteTemplate(@PathParam("templateId") String nameOrId) {

        nameOrId = Strings.nullToEmpty(nameOrId).trim();

        if (nameOrId.isEmpty()) {
            throw new NotFoundException();
        }

        getWalletService().deleteWallet(nameOrId);
    }

    public NeoWalletService getWalletService() {
        return neoWalletService;
    }

    @Inject
    public void setWalletService(NeoWalletService neoWalletService) {
        this.neoWalletService = neoWalletService;
    }
}
