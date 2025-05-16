package dev.getelements.elements.rest.google;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.CreateGooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("google")
public class GooglePlayIapReceiptResource {

    private GooglePlayIapReceiptService googlePlayIapReceiptService;

    private ValidationHelper validationHelper;

    @POST
    @Path("purchase")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Upload Google Play IAP Receipt data (package name, product id and Google Play-issued " +
            "purchase token). Returns a list of RewardIssuances, some or all of which may be already redeemed.")
    public List<RewardIssuance> uploadGooglePlayIapPurchase(
            final CreateGooglePlayIapReceipt createGooglePlayIapReceipt
    ) {
        getValidationHelper().validateModel(createGooglePlayIapReceipt, Create.class);
        final String packageName = createGooglePlayIapReceipt.getPackageName();
        final String productId = createGooglePlayIapReceipt.getProductId();
        final String purchaseToken = createGooglePlayIapReceipt.getPurchaseToken();

        if (packageName.length() == 0) {
            throw new InvalidDataException("Package Name must not be the empty string.");
        }

        if (productId.length() == 0) {
            throw new InvalidDataException("Product Id must not be the empty string.");
        }

        if (purchaseToken.length() == 0) {
            throw new InvalidDataException("Purchase Token must not be the empty string.");
        }

        final GooglePlayIapReceipt resultGooglePlayIapReceipt = getGooglePlayIapReceiptService()
                .verifyAndCreateGooglePlayIapReceiptIfNeeded(packageName, productId, purchaseToken);

        final List<RewardIssuance> resultRewardIssuances = getGooglePlayIapReceiptService()
                .getOrCreateRewardIssuances(resultGooglePlayIapReceipt);

        return resultRewardIssuances;
    }

    public GooglePlayIapReceiptService getGooglePlayIapReceiptService() {
        return googlePlayIapReceiptService;
    }

    @Inject
    public void setGooglePlayIapReceiptService(GooglePlayIapReceiptService googlePlayIapReceiptService) {
        this.googlePlayIapReceiptService = googlePlayIapReceiptService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
