package dev.getelements.elements.rest.meta.oculus;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("meta/oculus")
public class OculusIapResource {

    private OculusIapReceiptService oculusIapReceiptService;

    private ValidationHelper validationHelper;

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Fetches the oculus iap receipt with the matching database id.")
    public OculusIapReceipt getOculusIapReceipt(@PathParam("{id}") String id) {
        return getOculusIapReceiptService().getOculusIapReceipt(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Fetches the oculus iap receipt with the matching database id.")
    public Pagination<OculusIapReceipt> getOculusIapReceipts(@QueryParam("offset") @DefaultValue("0") int offset,
                                                                 @QueryParam("count") @DefaultValue("20") int count) {
        return getOculusIapReceiptService().getOculusIapReceipts(offset, count);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Attempts to create the Oculus IAP Receipt data.")
    public OculusIapReceipt createOculusIapReceipt(final OculusIapReceipt oculusIapReceipt) {
        validateReceipt(oculusIapReceipt);
        return getOculusIapReceiptService().getOrCreateOculusIapReceipt(oculusIapReceipt);
    }

    @POST
    @Path("consume")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Attempts to consume the Oculus IAP Receipt data. " +
            "Returns a list of RewardIssuances, some or all of which may be already redeemed.")
    public OculusIapConsumeResponse consumeOculusIapPurchase(final OculusIapReceipt oculusIapReceipt) {
        validateReceipt(oculusIapReceipt);
        return getOculusIapReceiptService().consumeAndRecordOculusIapReceipt(oculusIapReceipt);
    }

    @POST
    @Path("verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Attempts to verify the Oculus IAP Receipt data." +
            "Returns a list of RewardIssuances, some or all of which may be already redeemed.")
    public OculusIapVerifyReceiptResponse verifyOculusIapPurchase(final OculusIapReceipt oculusIapReceipt) {
        validateReceipt(oculusIapReceipt);
        return getOculusIapReceiptService().verifyAndCreateOculusIapReceiptIfNeeded(oculusIapReceipt);
    }

    @GET
    @Path("reward")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Attempts to verify the Oculus IAP Receipt data." +
            "Returns a list of RewardIssuances, some or all of which may be already redeemed.")
    public List<RewardIssuance> getRewardIssuances(OculusIapReceipt oculusIapReceipt) {
        validateReceipt(oculusIapReceipt);
        return getOculusIapReceiptService().getOrCreateRewardIssuances(oculusIapReceipt);
    }

    private void validateReceipt(OculusIapReceipt oculusIapReceipt) {

        getValidationHelper().validateModel(oculusIapReceipt, ValidationGroups.Create.class);

        final String purchaseId = oculusIapReceipt.getPurchaseId();
        final String fbUserId = oculusIapReceipt.getUserId();
        final String sku = oculusIapReceipt.getSku();

        if (purchaseId.isEmpty()) {
            throw new InvalidDataException("Purchase id must not be an empty string.");
        }

        if (fbUserId.isEmpty()) {
            throw new InvalidDataException("Oculus User Id must not be an empty string.");
        }

        if (sku.isEmpty()) {
            throw new InvalidDataException("SKU must not be an empty string.");
        }
    }

    public OculusIapReceiptService getOculusIapReceiptService() {
        return oculusIapReceiptService;
    }

    @Inject
    public void setOculusIapReceiptService(OculusIapReceiptService googlePlayIapReceiptService) {
        this.oculusIapReceiptService = googlePlayIapReceiptService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
