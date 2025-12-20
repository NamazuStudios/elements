package dev.getelements.elements.sdk.service.receipt;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.receipt.CreateReceiptRequest;
import dev.getelements.elements.sdk.model.receipt.Receipt;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ReceiptService {

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param userId   the id of the user to search for
     * @param offset the offset
     * @param count  the count
     * @param search Used to filter by scheme if you support multiple IAP providers
     * @return a {@link Pagination} of {@link Receipt} objects.
     */
    Pagination<Receipt> getReceipts(String userId, int offset, int count, String search);

    /**
     * Gets the receipt with the schema and transaction id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param schema the schema of the payment processor
     * @param originalTransactionId the original transaction id
     * @return the {@link Receipt} that was requested, never null
     */
    Receipt getReceiptBySchemaAndTransactionId(String schema, String originalTransactionId);

    /**
     * Gets the receipt with the db id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param id the db id
     * @return the {@link Receipt} that was requested, never null
     */
    Receipt getReceiptById(String id);

    /**
     * Creates a new receipt.
     *
     * @return the {@link Receipt} as it was written into the database
     * @throws InvalidDataException if the state of the passed in Receipt is invalid
     * @throws DuplicateException   if the passed in Receipt has a scheme + transaction id that already exists
     */
    Receipt createReceipt(CreateReceiptRequest receipt);

    /**
     * Deletes an existing receipt.
     *
     * @param receiptId the db id of the receipt
     */
    void deleteReceipt(String receiptId);

}
