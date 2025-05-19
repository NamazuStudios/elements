package dev.getelements.elements.rest.apple;

import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.appleiapreceipt.CreateAppleIapReceipt;
import dev.getelements.elements.sdk.model.appleiapreceipt.CreateAppleIapReceipt.CreateAppleIapReceiptEnvironment;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.checkerframework.checker.units.qual.C;

import java.util.List;

@Path("ios")
public class AppleIapReceiptResource {

    private AppleIapReceiptService appleIapReceiptService;

    private ValidationHelper validationHelper;

    @POST
    @Path("receipt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( description =
            "Upload Apple IAP Receipt. Returns a list of RewardIssuances, which may contain already-redeemed " +
            "issuances."
    )
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
