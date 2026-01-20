package dev.getelements.elements.rest.blockchain;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateWalletRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.UpdateWalletRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;
import dev.getelements.elements.sdk.service.blockchain.WalletService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import static com.google.common.base.Strings.emptyToNull;

@Path("blockchain/omni/vault/{vaultId}/wallet")
public class VaultWalletResource {

    private WalletService walletService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Gets  wallets. Optionally filtered for a specific user",
            description = "Gets a pagination of  Wallets. Optionally a user Id can be specified to filter for a given user.")
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
    @Operation(
            summary = "Creates a new  Wallet",
            description = "Creates a new  Wallet, associated with the given user.")
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
    @Operation(
            summary = "Updates a  Wallet",
            description = "Updates a  Wallet with the specified name or id.")
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
    @Operation(
            summary = "Gets a specific  Wallet",
            description = "Gets a specific  Wallet by Id.")
    public Wallet getWalletForVault(

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
    @Operation(
            summary = "Deletes a  Wallet",
            description = "Deletes a  Wallet with the specified id.")
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
