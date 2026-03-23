package dev.getelements.elements.rest.steam;

import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.steam.CreateSteamIapReceipt;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.steam.SteamIapReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("steam")
public class SteamIapReceiptResource {

    private SteamIapReceiptService steamIapReceiptService;

    private ValidationHelper validationHelper;

    @POST
    @Path("purchase")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Verify a Steam IAP purchase by order ID and return a list of RewardIssuances. " +
            "The order ID is validated against the Steam ISteamMicroTxn API using the publisher key and " +
            "app ID stored in the application's SteamApplicationConfiguration. " +
            "Some or all of the returned RewardIssuances may already be redeemed.")
    public List<RewardIssuance> uploadSteamIapPurchase(final CreateSteamIapReceipt createSteamIapReceipt) {

        getValidationHelper().validateModel(createSteamIapReceipt, Create.class);

        final var orderId = createSteamIapReceipt.getOrderId();

        if (orderId.isEmpty()) {
            throw new InvalidDataException("Order ID must not be the empty string.");
        }

        final var receipt = getSteamIapReceiptService().verifyAndCreateSteamIapReceiptIfNeeded(orderId);
        return getSteamIapReceiptService().getOrCreateRewardIssuances(receipt);
    }

    public SteamIapReceiptService getSteamIapReceiptService() {
        return steamIapReceiptService;
    }

    @Inject
    public void setSteamIapReceiptService(SteamIapReceiptService steamIapReceiptService) {
        this.steamIapReceiptService = steamIapReceiptService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
