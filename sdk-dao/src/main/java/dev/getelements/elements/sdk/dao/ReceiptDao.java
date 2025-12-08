package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;


@ElementServiceExport
public interface ReceiptDao {

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
     * Gets the receipt with the id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param originalTransactionId the original facebook transaction id
     * @return the {@link Receipt} that was requested, never null
     */
    Receipt getReceipt(String schema, String originalTransactionId);

    /**
     * Creates a new receipt.
     *
     * @return the {@link Receipt} as it was written into the database
     * @throws InvalidDataException if the state of the passed in FacebookIapReceipt is invalid
     * @throws DuplicateException   if the passed in FacebookIapReceipt has a name that already exists
     */
    Receipt getOrCreateReceipt(Receipt receipt);

    /**
     * Deletes an existing receipt.
     *
     * @param receiptId the db id of the receipt
     */
    void deleteReceipt(String receiptId);

}
