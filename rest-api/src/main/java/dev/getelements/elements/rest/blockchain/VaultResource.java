package dev.getelements.elements.rest.blockchain;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateVaultRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.UpdateVaultRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.service.blockchain.VaultService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

@Path("blockchain/omni/vault")
public class VaultResource {

    private VaultService vaultService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Gets  vaults. Optionally filtered for a specific user",
            description = "Gets a pagination of  Wallets. Optionally a user Id can be specified to filter for a given user.")
    public Pagination<Vault> getVaults(

            @QueryParam("offset")
            @DefaultValue("0")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            final int count,

            @QueryParam("userId")
            String userId

    ) {
        userId = emptyToNull(userId);
        return getVaultService().getVaults(offset, count, userId);
    }

    @GET
    @Path("{vaultId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a specific  Vault",
            description = "Gets a specific  Vault by Id.")
    public Vault getVault(@PathParam("vaultId") String vaultId) {
        vaultId = nullToEmpty(vaultId).trim();
        return getVaultService().getVault(vaultId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new  Vault",
            description = "Creates a new  Vault, associated with the given user.")
    public Vault createVault(final CreateVaultRequest request) {
        return getVaultService().createVault(request);
    }

    @PUT
    @Path("{vaultId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates a  Vault",
            description = "Updates a  Vault with the specified name or id.")
    public Vault updateVault(@PathParam("vaultId") final String vaultId, final UpdateVaultRequest request) {
        return getVaultService().updateVault(vaultId, request);
    }

    @DELETE
    @Path("{vaultId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a  Vault",
            description = "Deletes a  Vault with the specified id.")
    public void deleteVault(@PathParam("vaultId") final String vaultId) {
        getVaultService().deleteVault(vaultId);
    }

    public VaultService getVaultService() {
        return vaultService;
    }

    @Inject
    public void setVaultService(VaultService vaultService) {
        this.vaultService = vaultService;
    }

}
