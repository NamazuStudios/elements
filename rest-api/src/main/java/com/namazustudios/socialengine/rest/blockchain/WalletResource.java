package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
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

@Api(value = "Blockchain Wallets",
        description =
                "Allows for the storage and retrieval blockchain wallets. This is part of the Omni API, which " +
                "provides access to all support blockchains.",
        authorizations = {
            @Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/omni/wallet")
public class WalletResource {

    private WalletService walletService;

    @GET
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific  Wallet",
            notes = "Gets a specific  Wallet by Id.")
    public Wallet getWallet(@PathParam("walletId") String walletId) {
        walletId = Strings.nullToEmpty(walletId).trim();
        return getWalletService().getWallet(walletId);
    }

    @DELETE
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a  Wallet",
            notes = "Deletes a  Wallet with the specified id.")
    public void deleteWallet(@PathParam("walletId") final String walletId) {
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
