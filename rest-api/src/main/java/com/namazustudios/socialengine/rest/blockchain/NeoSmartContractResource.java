package com.namazustudios.socialengine.rest.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.service.blockchain.NeoSmartContractService;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

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
    public Pagination<NeoSmartContract> getContracts(
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
    public NeoSmartContract getContract(@PathParam("contractId") String contractId) {
        return getNeoSmartContractService().getNeoSmartContract(contractId);
    }

    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Patches a Neo Smart Contract",
            notes = "Patches a Neo Smart Contract entry, associated with the specified deployed script hash.")
    public NeoSmartContract patchContract(final PatchNeoSmartContractRequest request) {
        return getNeoSmartContractService().patchNeoSmartContract(request);
    }

    @POST
    @Path("mint")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mints a token using the specified contract.",
            notes = "Mints the specified token using the specified contract id.")
    public NeoSendRawTransaction mintToken(final MintTokenRequest request) {
        return getNeoSmartContractService().mintToken(request);
    }

    @POST
    @Path("invoke")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Invokes the specified method on the contract.",
            notes = "Invokes the specified method using the specified contract id.")
    public NeoSendRawTransaction invoke(final InvokeContractRequest request,
                              @QueryParam("method") String methodToInvoke,
                              @QueryParam("params") List<String> methodParams) {
        return getNeoSmartContractService().invoke(request, methodToInvoke, methodParams);
    }

    @POST
    @Path("invoke/test")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mints a token using the specified contract.",
            notes = "Mints the specified token using the specified contract id.")
    public NeoInvokeFunction testInvoke(final InvokeContractRequest request,
                                    @QueryParam("method") String methodToInvoke,
                                    @QueryParam("params") List<String> methodParams) {
        return getNeoSmartContractService().testInvoke(request, methodToInvoke, methodParams);
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
