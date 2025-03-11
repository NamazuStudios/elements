package dev.getelements.elements.rest.blockchain;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;

import dev.getelements.elements.sdk.service.blockchain.WalletService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("blockchain/omni/wallet")
public class WalletResource {

    private WalletService walletService;

    @GET
    @Path("{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a specific  Wallet",
            description = "Gets a specific  Wallet by Id.")
    public Wallet getWallet(@PathParam("walletId") String walletId) {
        walletId = Strings.nullToEmpty(walletId).trim();
        return getWalletService().getWallet(walletId);
    }

    public WalletService getWalletService() {
        return walletService;
    }

    @Inject
    public void setWalletService(WalletService neoWalletService) {
        this.walletService = neoWalletService;
    }

}
