package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainProtocol;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by keithhudnall on 9/21/21.
 */
@Api(value = "Blockchain Wallets",
        description =
                "Allows for the storage and retrieval blockchain wallets. This is part of the Omni API, which " +
                "provides access to all support blockchains.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/omni/wallet")
public class WalletResource {

    private WalletService walletService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets  wallets. Optionally filtered for a specific user",
            notes = "Gets a pagination of  Wallets. Optionally a user Id can be specified to filter for a given user.")
    public Pagination<Wallet> getWallets(

            @QueryParam("offset")
            @DefaultValue("0")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            final int count,

            @QueryParam("userId")
            String userId,

            @QueryParam("protocol")
            final BlockchainProtocol protocol,

            @QueryParam("network")
            final List<BlockchainNetwork> network

    ) {
        userId = emptyToNull(userId);
        return getWalletService().getWallets(offset, count, userId, protocol, network);
    }

    @GET
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific  Wallet",
            notes = "Gets a specific  Wallet by Id.")
    public Wallet getWallet(@PathParam("walletId") String walletId) {
        walletId = Strings.nullToEmpty(walletId).trim();
        return getWalletService().getWallet(walletId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new  Wallet",
            notes = "Creates a new  Wallet, associated with the given user.")
    public Wallet createWallet(final CreateWalletRequest request) {
        return getWalletService().createWallet(request);
    }

    @PUT
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a  Wallet",
            notes = "Updates a  Wallet with the specified name or id.")
    public Wallet updateWallet(@PathParam("walletId") final String walletId, final UpdateWalletRequest request) {
        return getWalletService().updateWallet(walletId, request);
    }

    @DELETE
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a  Wallet",
            notes = "Deletes a  Wallet with the specified id.")
    public void deleteTemplate(@PathParam("walletId") final String walletId) {
        getWalletService().deleteWallet(walletId);
    }

    public WalletService getWalletService() {
        return walletService;
    }

    @Inject
    public void setWalletService(WalletService neoWalletService) {
        this.walletService = neoWalletService;
    }

}
