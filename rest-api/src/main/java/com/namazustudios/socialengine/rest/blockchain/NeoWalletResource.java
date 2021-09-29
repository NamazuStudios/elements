package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.blockchain.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.Wallet;
import com.namazustudios.socialengine.model.blockchain.UpdateWalletRequest;
import com.namazustudios.socialengine.service.blockchain.WalletService;
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
@Path("blockchain/neo/wallet")
public class NeoWalletResource {

    private WalletService walletService;

    @GET
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Neo Smart Contract Template",
            notes = "Gets a specific Neo Smart Contract Template by templateId.")
    public Wallet getWallet(@PathParam("walletId") String walletId) {

        walletId = Strings.nullToEmpty(walletId).trim();

        if (walletId.isEmpty()) {
            throw new NotFoundException();
        }

        return getWalletService().getWallet(walletId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Neo Smart Contract Template",
            notes = "Creates a new Neo Smart Contract Template, associated with the specified application.")
    public Wallet createWallet(final CreateWalletRequest request) {
        return getWalletService().createWallet(request);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Neo Smart Contract Template",
            notes = "Updates a Neo Smart Contract Template with the specified name or id.")
    public Wallet updateWallet(final UpdateWalletRequest request) {
        return getWalletService().updateWallet(request);
    }

    @DELETE
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Neo Smart Contract Template",
            notes = "Deletes a Neo Smart Contract Template with the specified name or id.")
    public void deleteTemplate(@PathParam("templateId") String nameOrId) {

        nameOrId = Strings.nullToEmpty(nameOrId).trim();

        if (nameOrId.isEmpty()) {
            throw new NotFoundException();
        }

        getWalletService().deleteWallet(nameOrId);
    }

    public WalletService getWalletService() {
        return walletService;
    }

    @Inject
    public void setWalletService(WalletService walletService) {
        this.walletService = walletService;
    }
}
