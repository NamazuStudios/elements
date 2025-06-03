package dev.getelements.elements.rest.blockchain;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.contract.CreateSmartContractRequest;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContract;
import dev.getelements.elements.sdk.model.blockchain.contract.UpdateSmartContractRequest;
import dev.getelements.elements.sdk.service.blockchain.SmartContractService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("blockchain/omni/smart_contract")
public class SmartContractResource {

    private SmartContractService smartContractService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets  contracts.",
            description = "Gets a pagination of  SmartContracts.")
    public Pagination<SmartContract> getSmartContracts(

            @QueryParam("offset")
            @DefaultValue("0")
            final int offset,

            @QueryParam("count")
            @DefaultValue("20")
            final int count,

            @QueryParam("api")
            final BlockchainApi api,

            @QueryParam("network")
            final List<BlockchainNetwork> network
    ) {
        return getSmartContractService().getSmartContracts(offset, count, api, network);
    }

    @GET
    @Path("{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Gets a specific  Smart Contract",
            description = "Gets a specific  Smart Contract by contractId.")
    public SmartContract getSmartContract(@PathParam("contractId") String contractId) {
        return getSmartContractService().getSmartContract(contractId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Patches a  Smart Contract",
            description = "Patches a  Smart Contract entry, associated with the specified deployed script hash.")
    public SmartContract createSmartContract(final CreateSmartContractRequest createSmartContractRequest) {
        return getSmartContractService().createSmartContract(createSmartContractRequest);
    }

    @PUT
    @Path("{contractId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Patches a  Smart Contract",
            description = "Patches a  Smart Contract entry, associated with the specified deployed script hash.")
    public SmartContract updateSmartContract(
            @PathParam("contractId") String contractId,
            final UpdateSmartContractRequest updateSmartContractRequest) {
        return getSmartContractService().updateSmartContract(contractId, updateSmartContractRequest);
    }

    @DELETE
    @Path("{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Deletes a  Smart Contract",
            description = "Deletes a  Smart Contract with the specified contractId.")
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
