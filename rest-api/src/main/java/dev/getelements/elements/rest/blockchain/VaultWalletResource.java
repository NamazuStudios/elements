package dev.getelements.elements.rest.blockchain;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.model.blockchain.wallet.CreateWalletRequest;
import dev.getelements.elements.model.blockchain.wallet.UpdateWalletRequest;
import dev.getelements.elements.model.blockchain.wallet.Wallet;
import dev.getelements.elements.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "Blockchain Vaults",
        description =
                "Allows for the storage and retrieval blockchain vaults. This is part of the Omni API, which " +
                "provides access to all supported blockchains.",
        authorizations = {
                @Authorization(AUTH_BEARER),
                @Authorization(SESSION_SECRET),
                @Authorization(SOCIALENGINE_SESSION_SECRET)
        })
@Path("blockchain/omni/vault/{vaultId}/wallet")
public class VaultWalletResource {

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

            @QueryParam("api")
            final BlockchainApi api,

            @QueryParam("network")
            final List<BlockchainNetwork> network,

            @PathParam("vaultId")
            final String vaultId

    ) {
        userId = emptyToNull(userId);
        return getWalletService().getWallets(offset, count, vaultId, userId, api, network);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new  Wallet",
            notes = "Creates a new  Wallet, associated with the given user.")
    public Wallet createWallet(

            @PathParam("vaultId")
            final String vaultId,

            final CreateWalletRequest request

    ) {
        return getWalletService().createWallet(vaultId, request);
    }

    @PUT
    @Path("{walletId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a  Wallet",
            notes = "Updates a  Wallet with the specified name or id.")
    public Wallet updateWallet(

            @PathParam("vaultId")
            final String vaultId,

            @PathParam("walletId")
            final String walletId,

            final UpdateWalletRequest request

    ) {
        return getWalletService().updateWallet(vaultId, walletId, request);
    }

    @GET
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific  Wallet",
            notes = "Gets a specific  Wallet by Id.")
    public Wallet getWallet(

            @PathParam("vaultId")
            final String vaultId,

            @PathParam("walletId")
            final String walletId
    ) {
        return getWalletService().getWalletInVault(walletId, vaultId);
    }

    @DELETE
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a  Wallet",
            notes = "Deletes a  Wallet with the specified id.")
    public void deleteWallet(

            @PathParam("vaultId")
            final String vaultId,

            @PathParam("walletId")
            final String walletId
    ) {
        getWalletService().deleteWalletFromVault(walletId, vaultId);
    }

    public WalletService getWalletService() {
        return walletService;
    }

    @Inject
    public void setWalletService(WalletService walletService) {
        this.walletService = walletService;
    }

}
