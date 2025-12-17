package dev.getelements.elements.rest.meta.facebook;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.meta.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapVerifyReceiptResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("meta/facebook")
public class FacebookIapResource {

    private FacebookIapReceiptService facebookIapReceiptService;

    private ValidationHelper validationHelper;

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Fetches the facebook iap receipt with the matching database id.")
    public FacebookIapReceipt getFacebookIapReceipt(@PathParam("{id}") String id) {
        return getFacebookIapReceiptService().getFacebookIapReceipt(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Fetches the facebook iap receipt with the matching database id.")
    public Pagination<FacebookIapReceipt> getFacebookIapReceipts(@QueryParam("offset") @DefaultValue("0") int offset,
                                                                 @QueryParam("count") @DefaultValue("20") int count) {
        return getFacebookIapReceiptService().getFacebookIapReceipts(offset, count);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Attempts to create the Facebook IAP Receipt data.")
    public FacebookIapReceipt createFacebookIapReceipt(final FacebookIapReceipt facebookIapReceipt) {
        validateReceipt(facebookIapReceipt);
        return getFacebookIapReceiptService().getOrCreateFacebookIapReceipt(facebookIapReceipt);
    }

    @POST
    @Path("verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Attempts to verify the Facebook IAP Receipt data." +
            "Returns a list of RewardIssuances, some or all of which may be already redeemed.")
    public FacebookIapVerifyReceiptResponse verifyFacebookIapPurchase(final FacebookIapReceipt facebookIapReceipt) {
        validateReceipt(facebookIapReceipt);
        return getFacebookIapReceiptService().verifyAndCreateFacebookIapReceiptIfNeeded(facebookIapReceipt);
    }

    @GET
    @Path("reward")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Attempts to verify the Facebook IAP Receipt data." +
            "Returns a list of RewardIssuances, some or all of which may be already redeemed.")
    public List<RewardIssuance> getRewardIssuances(FacebookIapReceipt facebookIapReceipt) {
        validateReceipt(facebookIapReceipt);
        return getFacebookIapReceiptService().getOrCreateRewardIssuances(facebookIapReceipt);
    }

    private void validateReceipt(FacebookIapReceipt facebookIapReceipt) {

        getValidationHelper().validateModel(facebookIapReceipt, ValidationGroups.Create.class);

        final String purchaseId = facebookIapReceipt.getPurchaseId();
        final String fbUserId = facebookIapReceipt.getUserId();
        final String sku = facebookIapReceipt.getSku();

        if (purchaseId.isEmpty()) {
            throw new InvalidDataException("Purchase id must not be an empty string.");
        }

        if (fbUserId.isEmpty()) {
            throw new InvalidDataException("Facebook User Id must not be an empty string.");
        }

        if (sku.isEmpty()) {
            throw new InvalidDataException("SKU must not be an empty string.");
        }
    }

    public FacebookIapReceiptService getFacebookIapReceiptService() {
        return facebookIapReceiptService;
    }

    @Inject
    public void setFacebookIapReceiptService(FacebookIapReceiptService googlePlayIapReceiptService) {
        this.facebookIapReceiptService = googlePlayIapReceiptService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
