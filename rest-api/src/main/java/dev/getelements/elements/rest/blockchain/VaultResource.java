package dev.getelements.elements.rest.blockchain;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.wallet.*;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.VaultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

@Api(value = "Blockchain Vaults",
        description =
                "Allows for the storage and retrieval blockchain vaults. This is part of the Omni API, which " +
                "provides access to all supported blockchains.",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)
})
@Path("blockchain/omni/vault")
public class VaultResource {

    private VaultService vaultService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets  vaults. Optionally filtered for a specific user",
            notes = "Gets a pagination of  Wallets. Optionally a user Id can be specified to filter for a given user.")
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
    @ApiOperation(
            value = "Gets a specific  Vault",
            notes = "Gets a specific  Vault by Id.")
    public Vault getVault(@PathParam("vaultId") String vaultId) {
        vaultId = nullToEmpty(vaultId).trim();
        return getVaultService().getVault(vaultId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a new  Vault",
            notes = "Creates a new  Vault, associated with the given user.")
    public Vault createVault(final CreateVaultRequest request) {
        return getVaultService().createVault(request);
    }

    @PUT
    @Path("{vaultId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Updates a  Vault",
            notes = "Updates a  Vault with the specified name or id.")
    public Vault updateVault(@PathParam("vaultId") final String vaultId, final UpdateVaultRequest request) {
        return getVaultService().updateVault(vaultId, request);
    }

    @DELETE
    @Path("{vaultId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a  Vault",
            notes = "Deletes a  Vault with the specified id.")
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
