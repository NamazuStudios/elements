package dev.getelements.elements.rest.blockchain;

import com.google.common.base.Strings;
import dev.getelements.elements.model.blockchain.wallet.Wallet;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(value = "Blockchain Wallets",
        description =
                "Allows for the storage and retrieval blockchain wallets. This is part of the Omni API, which " +
                "provides access to all support blockchains.",
        authorizations = {
            @Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
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

    public WalletService getWalletService() {
        return walletService;
    }

    @Inject
    public void setWalletService(WalletService neoWalletService) {
        this.walletService = neoWalletService;
    }

}
