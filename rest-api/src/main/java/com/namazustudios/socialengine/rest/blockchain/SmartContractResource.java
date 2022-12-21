package com.namazustudios.socialengine.rest.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.UpdateSmartContractRequest;
import com.namazustudios.socialengine.service.blockchain.SmartContractService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "Smart Contracts",
        hidden = true,
        description = "Allows for the storage and retrieval of compiled Neo smart contracts.",
        authorizations = {
                @Authorization(AUTH_BEARER),
                @Authorization(SESSION_SECRET),
                @Authorization(SOCIALENGINE_SESSION_SECRET)
        }
)
@Path("blockchain/omni/smart_contract")
public class SmartContractResource {

    private SmartContractService smartContractService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets  contracts.",
            notes = "Gets a pagination of  SmartContracts.")
    public Pagination<SmartContract> getContracts(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") @DefaultValue("") String search) {
        return getSmartContractService().getSmartContracts(offset, count, search);
    }

    @GET
    @Path("{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific  Smart Contract",
            notes = "Gets a specific  Smart Contract by contractId.")
    public SmartContract getContract(@PathParam("contractId") String contractId) {
        return getSmartContractService().getSmartContract(contractId);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Patches a  Smart Contract",
            notes = "Patches a  Smart Contract entry, associated with the specified deployed script hash.")
    public SmartContract patchContract(final UpdateSmartContractRequest request) {
        return getSmartContractService().updateSmartContract(request);
    }

    @DELETE
    @Path("{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a  Smart Contract",
            notes = "Deletes a  Smart Contract with the specified contractId.")
    public void deleteContract(@PathParam("contractId") String contractId) {
        getSmartContractService().deleteContract(contractId);
    }

    public SmartContractService getSmartContractService() {
        return smartContractService;
    }

    @Inject
    public void setSmartContractService(SmartContractService bscSmartContractService) {
        this.smartContractService = bscSmartContractService;
    }

}
