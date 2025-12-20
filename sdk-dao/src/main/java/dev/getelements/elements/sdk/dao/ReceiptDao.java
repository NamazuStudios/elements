package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;


@ElementServiceExport
@ElementEventProducer(
        value = ReceiptDao.RECEIPT_CREATED,
        parameters = Receipt.class,
        description = "Called when a new receipt is created. " +
                "If the raw receipt data needs to be parsed, check the scheme to determine the corresponding class:\n" +
                "GOOGLE_IAP_SCHEME -> GooglePlayIapReceipt\n" +
                "OCULUS_PLATFORM_IAP_SCHEME -> OculusIapReceipt\n" +
                "APPLE_IAP_SCHEME -> AppleIapReceipt"
)
@ElementEventProducer(
        value = ReceiptDao.RECEIPT_CREATED,
        parameters = {Receipt.class, Transaction.class},
        description = "Called when a new receipt is created. " +
                "If the raw receipt data needs to be parsed, check the scheme to determine the corresponding class:\n" +
                "GOOGLE_IAP_SCHEME -> GooglePlayIapReceipt\n" +
                "OCULUS_PLATFORM_IAP_SCHEME -> OculusIapReceipt\n" +
                "APPLE_IAP_SCHEME -> AppleIapReceipt"
)
public interface ReceiptDao {

    String RECEIPT_CREATED = "dev.getelements.elements.sdk.model.dao.receipt.created";

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param user   the user
     * @param offset the offset
     * @param count  the count
     * @param search Used to filter by scheme if you support multiple IAP providers
     * @return a {@link Pagination} of {@link Receipt} objects.
     */
    Pagination<Receipt> getReceipts(User user, int offset, int count, String search);

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param user   the user
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link Receipt} objects.
     */
    Pagination<Receipt> getReceipts(User user, int offset, int count);

    /**
     * Gets the receipt with the matching database id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param id the database id
     * @return the {@link Receipt} that was requested, never null
     */
    Receipt getReceipt(String id);

    /**
     * Gets the receipt with the matching schema and transaction id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param originalTransactionId the original transaction id
     * @return the {@link Receipt} that was requested, never null
     */
    Receipt getReceipt(String schema, String originalTransactionId);

    /**
     * Creates a new receipt.
     *
     * @return the {@link Receipt} as it was written into the database
     * @throws InvalidDataException if the state of the passed in Receipt is invalid
     * @throws DuplicateException   if the passed in Receipt has a schema + id that already exists
     */
    Receipt createReceipt(Receipt receipt);

    /**
     * Deletes an existing receipt.
     *
     * @param receiptId the db id of the receipt
     */
    void deleteReceipt(String receiptId);

}
