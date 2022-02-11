package com.namazustudios.socialengine.rest.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.model.blockchain.neo.MintNeoTokenResponse;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import com.namazustudios.socialengine.service.blockchain.NeoSmartContractService;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
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
@Api(value = "Neo Smart Contract",
        description = "Allows for the storage and retrieval of compiled Neo smart contracts.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/neo/contract")
public class NeoSmartContractResource {

    private NeoSmartContractService neoSmartContractService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets Neo contracts.",
            notes = "Gets a pagination of Neo Contracts.")
    public Pagination<ElementsSmartContract> getContracts(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") @DefaultValue("") String search) {
        return getNeoSmartContractService().getNeoSmartContracts(offset, count, search);
    }

    @GET
    @Path("{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Neo Smart Contract",
            notes = "Gets a specific Neo Smart Contract by contractId.")
    public ElementsSmartContract getContract(@PathParam("contractId") String contractId) {
        return getNeoSmartContractService().getNeoSmartContract(contractId);
    }

    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Patches a Neo Smart Contract",
            notes = "Patches a Neo Smart Contract entry, associated with the specified deployed script hash.")
    public ElementsSmartContract patchContract(final PatchSmartContractRequest request) {
        return getNeoSmartContractService().patchNeoSmartContract(request);
    }

    @POST
    @Path("mint")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mints a token using the specified contract.",
            notes = "Mints the specified token using the specified contract id.",
            response = MintNeoTokenResponse.class)
    public void mintToken(final MintTokenRequest request,
                          @Suspended final AsyncResponse asyncResponse) {
        getNeoSmartContractService().mintToken(
            request,
            m -> asyncResponse.resume(m == null ? Response.status(NOT_FOUND).build() : m),
            asyncResponse::resume);
    }

    @POST
    @Path("invocation")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Invokes the specified method on the contract.",
            notes = "Invokes the specified method using the specified contract id.",
            response = NeoSendRawTransaction.class)
    public void invoke(final InvokeContractRequest request,
                       @Suspended final AsyncResponse asyncResponse) {
        getNeoSmartContractService().invoke(
            request,
            m -> asyncResponse.resume(m == null ? Response.status(NOT_FOUND).build() : m),
            asyncResponse::resume);
    }

    @POST
    @Path("invocation_test")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Tests the invocation of the specified method on the contract without incurring GAS fees.",
            notes = "Invokes the specified method using the specified contract id.")
    public NeoInvokeFunction testInvoke(
            final InvokeContractRequest request,
            @Suspended
            final AsyncResponse asyncResponse) {
        return getNeoSmartContractService().testInvoke(request);
    }

    @DELETE
    @Path("{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Neo Smart Contract",
            notes = "Deletes a Neo Smart Contract with the specified contractId.")
    public void deleteContract(@PathParam("contractId") String contractId) {
        getNeoSmartContractService().deleteContract(contractId);
    }

    public NeoSmartContractService getNeoSmartContractService() {
        return neoSmartContractService;
    }

    @Inject
    public void setNeoSmartContractService(NeoSmartContractService neoSmartContractService) {
        this.neoSmartContractService = neoSmartContractService;
    }
}
