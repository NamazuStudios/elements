package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.ValidationGroups.Create;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import com.namazustudios.socialengine.model.appleiapreceipt.CreateAppleIapReceipt;
import com.namazustudios.socialengine.model.appleiapreceipt.CreateAppleIapReceipt.CreateAppleIapReceiptEnvironment;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptService;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Api(value = "Apple IAPs",
     description = "A REST interface where Apple IAP receipts are POSTed for the purposes of generating " +
             "RewardIssuances.",
     authorizations = {@Authorization(SESSION_SECRET)})
@Path("ios")
public class AppleIapReceiptResource {

    private AppleIapReceiptService appleIapReceiptService;

    private ValidationHelper validationHelper;

    @POST
    @Path("receipt")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Upload Apple IAP Receipt. Returns a list of RewardIssuances, which may contain already-" +
            "redeemed issuances.")
    public List<RewardIssuance> uploadAppleIapReceipt(final CreateAppleIapReceipt createAppleIapReceipt) {
        getValidationHelper().validateModel(createAppleIapReceipt, Create.class);
        final String receiptData = createAppleIapReceipt.getReceiptData();

        final CreateAppleIapReceiptEnvironment createAppleIapReceiptEnvironment =
                createAppleIapReceipt.getCreateAppleIapReceiptEnvironment();

        final AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment;

        switch (createAppleIapReceiptEnvironment) {
            case PRODUCTION:
                appleIapVerifyReceiptEnvironment = AppleIapVerifyReceiptEnvironment.PRODUCTION;
                break;
            case SANDBOX:
            default:
                appleIapVerifyReceiptEnvironment = AppleIapVerifyReceiptEnvironment.SANDBOX;
                break;
        }

        final List<AppleIapReceipt> resultAppleIapReceipts = getAppleIapReceiptService()
                .verifyAndCreateAppleIapReceiptsIfNeeded(appleIapVerifyReceiptEnvironment, receiptData);

        final List<RewardIssuance> resultRewardIssuances = getAppleIapReceiptService()
                .getOrCreateRewardIssuances(resultAppleIapReceipts);

        return resultRewardIssuances;
    }

    public AppleIapReceiptService getAppleIapReceiptService() {
        return appleIapReceiptService;
    }

    @Inject
    public void setAppleIapReceiptService(AppleIapReceiptService appleIapReceiptService) {
        this.appleIapReceiptService = appleIapReceiptService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
