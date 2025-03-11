package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;


@ElementServiceExport
public interface GooglePlayIapReceiptDao {

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
