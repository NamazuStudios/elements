package dev.getelements.elements.dao;

import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

@Expose({
        @ModuleDefinition("eci.elements.dao.googleplayiapreceipt"),
        @ModuleDefinition(
                value = "namazu.elements.dao.googleplayiapreceipt",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.googleplayiapreceipt instead")
        ),
        @ModuleDefinition(
                value = "namazu.socialengine.dao.googleplayiapreceipt",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.googleplayiapreceipt instead")
        )
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
