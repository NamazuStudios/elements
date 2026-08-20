package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;


@ElementServiceExport
@ElementEventProducer(
        value = AppleIapReceiptDao.APPLE_IAP_RECEIPT_CREATED,
        parameters = AppleIapReceipt.class,
        description = "Called when a new Apple IAP receipt is created."
)
@ElementEventProducer(
        value = AppleIapReceiptDao.APPLE_IAP_RECEIPT_CREATED,
        parameters = {AppleIapReceipt.class, Transaction.class},
        description = "Called when a new Apple IAP receipt is created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = AppleIapReceiptDao.APPLE_IAP_RECEIPT_DELETED,
        parameters = AppleIapReceipt.class,
        description = "Called when an Apple IAP receipt is deleted."
)
@ElementEventProducer(
        value = AppleIapReceiptDao.APPLE_IAP_RECEIPT_DELETED,
        parameters = {AppleIapReceipt.class, Transaction.class},
        description = "Called when an Apple IAP receipt is deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface AppleIapReceiptDao {

    String APPLE_IAP_RECEIPT_CREATED = "dev.getelements.elements.sdk.model.dao.appleiapreceipt.created";

    String APPLE_IAP_RECEIPT_DELETED = "dev.getelements.elements.sdk.model.dao.appleiapreceipt.deleted";

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param user   the user
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link AppleIapReceipt} objects.
     */
    Pagination<AppleIapReceipt> getAppleIapReceipts(User user, int offset, int count);

    /**
     * Gets the receipt with the id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param originalTransactionId the original apple transaction id
     * @return the {@link AppleIapReceipt} that was requested, never null
     */
    AppleIapReceipt getAppleIapReceipt(String originalTransactionId);

    /**
     * Creates a new receipt.
     *
     * @return the {@link AppleIapReceipt} as it was written into the database
     * @throws InvalidDataException if the state of the passed in AppleIapReceipt is invalid
     * @throws DuplicateException   if the passed in AppleIapReceipt has a name that already exists
     */
    AppleIapReceipt getOrCreateAppleIapReceipt(AppleIapReceipt appleIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param originalTransactionId the original apple transaction id
     */
    void deleteAppleIapReceipt(String originalTransactionId);

}
