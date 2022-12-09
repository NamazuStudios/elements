package com.namazustudios.socialengine.rest.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.service.blockchain.BscWalletService;
import com.namazustudios.socialengine.util.ValidationHelper;
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
@Api(value = "Bsc Wallets",
        description = "Allows for the storage and retrieval of compiled Bsc wallets.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/bsc/wallet")
public class BscWalletResource {

    private BscWalletService bscWalletService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Bsc wallets. Optionally filtered for a specific user",
            notes = "Gets a pagination of Bsc Wallets. Optionally a user Id can be specified to filter for a given user.")
    public Pagination<BscWallet> getWallets(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("userId") final String userId,
            @QueryParam("format") @DefaultValue("NONE") final String format) {

        return getWalletService().getWallets(offset, count, userId);
    }
    
    @GET
    @Path("{walletNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Bsc Wallet",
            notes = "Gets a specific Bsc Wallet by Id.")
    public BscWallet getWallet(@PathParam("walletNameOrId") String walletNameOrId) {
        return getWalletService().getWallet(walletNameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Bsc Wallet",
            notes = "Creates a new Bsc Wallet, associated with the given user.")
    public BscWallet createWallet(final CreateBscWalletRequest request) {
        getValidationHelper().validateModel(request);
        return getWalletService().createWallet(request);
    }

    @PUT
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Bsc Wallet",
            notes = "Updates a Bsc Wallet with the specified name or id.")
    public BscWallet updateWallet(@PathParam("walletId") String walletId, final UpdateBscWalletRequest request) {
        getValidationHelper().validateModel(request);
        return getWalletService().updateWallet(walletId, request);
    }

    @DELETE
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Bsc Wallet",
            notes = "Deletes a Bsc Wallet with the specified id.")
    public void deleteTemplate(@PathParam("walletId") String walletId) {
        getWalletService().deleteWallet(walletId);
    }

    public BscWalletService getWalletService() {
        return bscWalletService;
    }

    @Inject
    public void setWalletService(BscWalletService bscWalletService) {
        this.bscWalletService = bscWalletService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
