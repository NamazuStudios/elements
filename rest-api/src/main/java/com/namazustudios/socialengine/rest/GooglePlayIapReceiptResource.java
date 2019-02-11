package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.googleplayiapreceipt.CreateGooglePlayIapReceipt;
import com.namazustudios.socialengine.model.googleplayiapreceipt.GooglePlayIapReceipt;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(value = "Google Play IAPs",
     description = "A REST interface where Google Play IAP receipt tokens are POSTed for the purposes of generating " +
             "RewardIssuances. N.B.: the REST interface does not directly change the consumption state of " +
             "a purchased product (it does not appear to be built into the Google API yet). Therefore, the expected " +
             "lifecycle is: user purchases product on FE, FE calls this verify receipt REST API and " +
             "receives a RewardIssuance, then the mobile app should submit a consumption request " +
             "to Google Play (assuming the product may be purchased more than once). I.e., the product is considered " +
             "consumed as soon as an issuance has been successfully generated on the server.",
     authorizations = {@Authorization(SESSION_SECRET)})
@Path("google")
public class GooglePlayIapReceiptResource {

    private GooglePlayIapReceiptService googlePlayIapReceiptService;

    private ValidationHelper validationHelper;

    @POST
    @Path("purchase")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Upload Google Play IAP Receipt data (package name, product id and Google Play-issued " +
            "purchase token). Returns a RewardIssuance, which may be already redeemed.")
    public RewardIssuance uploadGooglePlayIapPurchase(
            final CreateGooglePlayIapReceipt createGooglePlayIapReceipt
    ) {
        getValidationHelper().validateModel(createGooglePlayIapReceipt, Create.class);
        final String packageName = createGooglePlayIapReceipt.getPackageName();
        final String productId = createGooglePlayIapReceipt.getProductId();
        final String purchaseToken = createGooglePlayIapReceipt.getPurchaseToken();

        final GooglePlayIapReceipt resultGooglePlayIapReceipt = getGooglePlayIapReceiptService()
                .verifyAndCreateGooglePlayIapReceiptIfNeeded(packageName, productId, purchaseToken);

        final RewardIssuance resultRewardIssuance = getGooglePlayIapReceiptService()
                .getOrCreateRewardIssuance(resultGooglePlayIapReceipt);

        return resultRewardIssuance;
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
