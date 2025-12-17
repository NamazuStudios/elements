package dev.getelements.elements.sdk.service.googleplayiap;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

@ElementPublic
@ElementServiceExport
@ElementEventProducer(
        value = ReceiptDao.RECEIPT_CREATED,
        parameters = GooglePlayIapReceipt.class,
        description = "Called when a new Google Play receipt is created."
)
public interface GooglePlayIapReceiptService {

    String GOOGLE_IAP_SCHEME = "com.android.vending";

    String GOOGLE_PLAY_IAP_RECEIPT_CREATED = "dev.getelements.elements.sdk.service.receipt.google.play.created";

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param user the user
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link GooglePlayIapReceipt} objects.
     */
    Pagination<GooglePlayIapReceipt> getGooglePlayIapReceipts(User user, int offset, int count);

    /**
     * Gets the receipt with the id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param orderId the original Google play order id.
     * @return the {@link GooglePlayIapReceipt} that was requested, never null
     */
    GooglePlayIapReceipt getGooglePlayIapReceipt(String orderId);

    /**
     * Finds a receipt in the db, or creates a new one if necessary.
     *
     * @return the {@link GooglePlayIapReceipt} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in receipt is invalid
     * @throws DuplicateException
     *     if the passed in receipt has a name that already exists
     */
    GooglePlayIapReceipt getOrCreateGooglePlayIapReceipt(GooglePlayIapReceipt googlePlayIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param orderId the original order id from Google Play
     */
    void deleteGooglePlayIapReceipt(String orderId);

    /**
     * Verifies the given purchase data against the Google Play purchase validation services.
     *
     * @param packageName the package name of the application, e.g. `com.namazustudios.example_app`.
     * @param productId the product id purchased by the user, e.g. `com.namazustudios.example_app.pack_10_coins`.
     * @param purchaseToken the token issued to the user upon successful Google Play purchase transaction.
     * @return the {@link GooglePlayIapReceipt} as it was written into the database, or the existing database record.
     */
    GooglePlayIapReceipt verifyAndCreateGooglePlayIapReceiptIfNeeded(
            String packageName,
            String productId,
            String purchaseToken);

    /**
     * Gets or creates a list of {@link RewardIssuance}s in the db for the given {@link GooglePlayIapReceipt}.
     *
     * @param googlePlayIapReceipt
     * @return
     */
    List<RewardIssuance> getOrCreateRewardIssuances(GooglePlayIapReceipt googlePlayIapReceipt);
}
