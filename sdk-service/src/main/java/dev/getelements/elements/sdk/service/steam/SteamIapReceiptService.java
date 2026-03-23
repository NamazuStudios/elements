package dev.getelements.elements.sdk.service.steam;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.steam.SteamIapReceipt;
import dev.getelements.elements.sdk.model.user.User;

import java.util.List;

@ElementPublic
@ElementServiceExport
@ElementEventProducer(
        value = SteamIapReceiptService.STEAM_IAP_RECEIPT_CREATED,
        parameters = SteamIapReceipt.class,
        description = "Called when a new Steam IAP receipt is created."
)
public interface SteamIapReceiptService {

    String STEAM_IAP_SCHEME = "com.valvesoftware.steam";

    String STEAM_IAP_RECEIPT_CREATED = "dev.getelements.elements.sdk.service.receipt.steam.created";

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param user the user
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link SteamIapReceipt} objects.
     */
    Pagination<SteamIapReceipt> getSteamIapReceipts(User user, int offset, int count);

    /**
     * Gets the receipt with the given order ID, or throws a {@link NotFoundException} if the
     * receipt cannot be found.
     *
     * @param orderId the Steam order ID
     * @return the {@link SteamIapReceipt} that was requested, never null
     */
    SteamIapReceipt getSteamIapReceipt(String orderId);

    /**
     * Finds a receipt in the database, or creates a new one if necessary.
     *
     * @param steamIapReceipt the receipt to find or create
     * @return the {@link SteamIapReceipt} as it was written into the database
     * @throws InvalidDataException if the state of the passed-in receipt is invalid
     * @throws DuplicateException if the passed-in receipt already exists
     */
    SteamIapReceipt getOrCreateSteamIapReceipt(SteamIapReceipt steamIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param orderId the Steam order ID
     */
    void deleteSteamIapReceipt(String orderId);

    /**
     * Verifies the given Steam order ID against the Steam ISteamMicroTxn API, then creates
     * a receipt if the transaction is valid and one does not already exist.
     *
     * @param orderId the Steam order ID from the client purchase transaction
     * @return the {@link SteamIapReceipt} as it was written into the database, or the existing record
     */
    SteamIapReceipt verifyAndCreateSteamIapReceiptIfNeeded(String orderId);

    /**
     * Gets or creates a list of {@link RewardIssuance}s in the database for the given {@link SteamIapReceipt}.
     *
     * @param steamIapReceipt the receipt for which to issue rewards
     * @return list of reward issuances
     */
    List<RewardIssuance> getOrCreateRewardIssuances(SteamIapReceipt steamIapReceipt);

}
