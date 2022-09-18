package com.namazustudios.socialengine.rest.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.model.blockchain.bsc.MintBscTokenResponse;
import com.namazustudios.socialengine.service.blockchain.BscSmartContractService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Created by keithhudnall on 9/21/21.
 */
@Api(value = "Bsc Smart Contract",
        description = "Allows for the storage and retrieval of compiled Bsc smart contracts.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/bsc/contract")
public class BscSmartContractResource {

    private BscSmartContractService bscSmartContractService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Bsc contracts.",
            notes = "Gets a pagination of Bsc Contracts.")
    public Pagination<ElementsSmartContract> getContracts(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") @DefaultValue("") String search) {
        return getBscSmartContractService().getBscSmartContracts(offset, count, search);
    }

    @GET
    @Path("{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Bsc Smart Contract",
            notes = "Gets a specific Bsc Smart Contract by contractId.")
    public ElementsSmartContract getContract(@PathParam("contractId") String contractId) {
        return getBscSmartContractService().getBscSmartContract(contractId);
    }

    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Patches a Bsc Smart Contract",
            notes = "Patches a Bsc Smart Contract entry, associated with the specified deployed script hash.")
    public ElementsSmartContract patchContract(final PatchSmartContractRequest request) {
        return getBscSmartContractService().patchBscSmartContract(request);
    }

    @POST
    @Path("mint")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mints a token using the specified contract.",
            notes = "Mints the specified token using the specified contract id.",
            response = MintBscTokenResponse.class)
    public void mintToken(final MintTokenRequest request,
                          @Suspended final AsyncResponse asyncResponse) {

        final var operation = getBscSmartContractService().mintToken(
            request,
            m -> asyncResponse.resume(m == null ? Response.status(NOT_FOUND).build() : m),
            asyncResponse::resume);

        asyncResponse.setTimeoutHandler(response -> operation.close());
    }

    @POST
    @Path("send")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Sends a transaction to the specified method on the contract.",
            notes = "Sends a transaction to the specified method using the specified contract id.",
            response = EVMInvokeContractResponse.class)
    public void send(final EVMInvokeContractRequest request,
                       @Suspended
                       final AsyncResponse asyncResponse) {

        final var operation = getBscSmartContractService().send(
            request,
            (response) -> asyncResponse.resume(response),
            asyncResponse::resume);

        asyncResponse.setTimeoutHandler(response -> operation.close());
    }

    @POST
    @Path("call")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Calls the specified method on the contract.",
            notes = "Calls the specified method using the specified contract id.",
            response = List.class)
    public void call(final EVMInvokeContractRequest request,
                       @Suspended
                       final AsyncResponse asyncResponse) {

        final var operation = getBscSmartContractService().call(
                request,
                (response) -> asyncResponse.resume(response),
                asyncResponse::resume);

        asyncResponse.setTimeoutHandler(response -> operation.close());
    }

    @DELETE
    @Path("{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Bsc Smart Contract",
            notes = "Deletes a Bsc Smart Contract with the specified contractId.")
    public void deleteContract(@PathParam("contractId") String contractId) {
        getBscSmartContractService().deleteContract(contractId);
    }

    public BscSmartContractService getBscSmartContractService() {
        return bscSmartContractService;
    }

    @Inject
    public void setBscSmartContractService(BscSmartContractService bscSmartContractService) {
        this.bscSmartContractService = bscSmartContractService;
    }
}
