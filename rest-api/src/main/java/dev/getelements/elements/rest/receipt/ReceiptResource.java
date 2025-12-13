package dev.getelements.elements.rest.receipt;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.service.receipt.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("receipt")
public class ReceiptResource {

    private ReceiptService receiptService;

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Receipt by its id.",
            description = "Gets a Receipt using the database id provided in the URL path.")
    public Receipt getReceipt(@PathParam("id") String id) {
        return receiptService.getReceiptById(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Receipt pagination.",
            description = "Retrieves a pagination of Receipt objects for the given user (SUPERUSER level), or the current user (USER level)." +
                    "Can search by originalTransactionId or scheme.")
    public Pagination<Receipt> getReceipts(@QueryParam("userId") String userId,
                                           @QueryParam("offset") @DefaultValue("0") int offset,
                                           @QueryParam("count") @DefaultValue("20") int count,
                                           @QueryParam("search") String search) {

        return receiptService.getReceipts(userId, offset, count, search);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a Receipt",
            description = "Creates a new Receipt record.")
    public Receipt createReceipt(Receipt receipt) {
        return receiptService.createReceipt(receipt);
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Deletes a Receipt",
            description = "Deletes and permanently removes the Receipt from the server.")
    public void deleteReceipt(@PathParam("id") String id) {
        receiptService.deleteReceipt(id);
    }

    public ReceiptService getReceiptService() {
        return receiptService;
    }

    @Inject
    public void setReceiptService(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

}
