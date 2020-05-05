package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.googleplayiapreceipt.GooglePlayIapReceipt;
import com.namazustudios.socialengine.rt.annotation.Expose;

@Expose(modules = {
        "namazu.elements.dao.googleplayiapreceipt",
        "namazu.socialengine.dao.googleplayiapreceipt",
})
public interface GooglePlayIapReceiptDao {

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
     * @param orderId the order id issued by Google Play services
     * @return the {@link GooglePlayIapReceipt} that was requested, never null
     */
    GooglePlayIapReceipt getGooglePlayIapReceipt(String orderId);

    /**
     * Creates a new receipt.
     *
     * @return the {@link GooglePlayIapReceipt} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in GooglePlayIapReceipt is invalid
     * @throws DuplicateException
     *     if the passed in GooglePlayIapReceipt has a name that already exists
     */
    GooglePlayIapReceipt getOrCreateGooglePlayIapReceipt(GooglePlayIapReceipt googlePlayIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param orderId the order id
     */
    void deleteGooglePlayIapReceipt(String orderId);

}
