package dev.getelements.elements.rest;

import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.model.appleiapreceipt.CreateAppleIapReceipt;
import dev.getelements.elements.model.appleiapreceipt.CreateAppleIapReceipt.CreateAppleIapReceiptEnvironment;
import dev.getelements.elements.model.reward.RewardIssuance;
import dev.getelements.elements.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api(value = "Apple IAPs",
     description = "A REST interface where Apple IAP receipts are POSTed for the purposes of generating " +
             "RewardIssuances.",
     authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
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

        if (createAppleIapReceiptEnvironment == null) {
            appleIapVerifyReceiptEnvironment = null;
        } else {
            switch (createAppleIapReceiptEnvironment) {
                case PRODUCTION:
                    appleIapVerifyReceiptEnvironment = AppleIapVerifyReceiptEnvironment.PRODUCTION;
                    break;
                case SANDBOX:
                default:
                    appleIapVerifyReceiptEnvironment = AppleIapVerifyReceiptEnvironment.SANDBOX;
                    break;
            }
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
