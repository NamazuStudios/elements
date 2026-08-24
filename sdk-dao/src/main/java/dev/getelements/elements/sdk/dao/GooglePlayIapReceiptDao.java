package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;


@ElementServiceExport
@ElementEventProducer(
        value = GooglePlayIapReceiptDao.GOOGLE_PLAY_IAP_RECEIPT_CREATED,
        parameters = GooglePlayIapReceipt.class,
        description = "Called when a new Google Play IAP receipt is created."
)
@ElementEventProducer(
        value = GooglePlayIapReceiptDao.GOOGLE_PLAY_IAP_RECEIPT_CREATED,
        parameters = {GooglePlayIapReceipt.class, Transaction.class},
        description = "Called when a new Google Play IAP receipt is created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = GooglePlayIapReceiptDao.GOOGLE_PLAY_IAP_RECEIPT_DELETED,
        parameters = GooglePlayIapReceipt.class,
        description = "Called when a Google Play IAP receipt is deleted."
)
@ElementEventProducer(
        value = GooglePlayIapReceiptDao.GOOGLE_PLAY_IAP_RECEIPT_DELETED,
        parameters = {GooglePlayIapReceipt.class, Transaction.class},
        description = "Called when a Google Play IAP receipt is deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface GooglePlayIapReceiptDao {

    String GOOGLE_PLAY_IAP_RECEIPT_CREATED = "dev.getelements.elements.sdk.model.dao.googleplayiapreceipt.created";

    String GOOGLE_PLAY_IAP_RECEIPT_DELETED = "dev.getelements.elements.sdk.model.dao.googleplayiapreceipt.deleted";

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param user   the user
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link GooglePlayIapReceipt} objects.
     */
    Pagination<GooglePlayIapReceipt> getGooglePlayIapReceipts(User user, int offset, int count);

    /**
     * Gets the receipt with the id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param orderId the order id issued by Google Play services
     * @return the {@link GooglePlayIapReceipt} that was requested, never null
     */
    GooglePlayIapReceipt getGooglePlayIapReceipt(String orderId);

    /**
     * Creates a new receipt.
     *
     * @return the {@link GooglePlayIapReceipt} as it was written into the database
     * @throws InvalidDataException if the state of the passed in GooglePlayIapReceipt is invalid
     * @throws DuplicateException   if the passed in GooglePlayIapReceipt has a name that already exists
     */
    GooglePlayIapReceipt getOrCreateGooglePlayIapReceipt(GooglePlayIapReceipt googlePlayIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param orderId the order id
     */
    void deleteGooglePlayIapReceipt(String orderId);

}
