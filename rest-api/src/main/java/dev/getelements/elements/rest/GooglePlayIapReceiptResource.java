package dev.getelements.elements.rest;

import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.googleplayiapreceipt.CreateGooglePlayIapReceipt;
import dev.getelements.elements.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.model.reward.RewardIssuance;
import dev.getelements.elements.service.googleplayiap.GooglePlayIapReceiptService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

@Api(value = "Google Play IAPs",
     description = "A REST interface where Google Play IAP receipt tokens are POSTed for the purposes of generating " +
             "RewardIssuances. N.B.: the REST interface does not directly change the consumption state of " +
             "a purchased product (it does not appear to be built into the Google API yet). Therefore, the expected " +
             "lifecycle is: user purchases product on FE, FE calls this verify receipt REST API and " +
             "receives a RewardIssuance, then the mobile app should submit a consumption request " +
             "to Google Play (assuming the product may be purchased more than once). I.e., the product is considered " +
             "consumed as soon as an issuance has been successfully generated on the server.",
     authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("google")
public class GooglePlayIapReceiptResource {

    private GooglePlayIapReceiptService googlePlayIapReceiptService;

    private ValidationHelper validationHelper;

    @POST
    @Path("purchase")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Upload Google Play IAP Receipt data (package name, product id and Google Play-issued " +
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
