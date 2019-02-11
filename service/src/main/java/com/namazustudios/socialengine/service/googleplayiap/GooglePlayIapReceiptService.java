package com.namazustudios.socialengine.service.googleplayiap;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.googleplayiapreceipt.GooglePlayIapReceipt;
import com.namazustudios.socialengine.model.reward.RewardIssuance;

import java.util.List;


public interface GooglePlayIapReceiptService {

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
     * @param orderId the original apple transaction id
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
     * If no existing record is found, verifies the given product id and purchase token against the Google Play servers
     * and inserts the receipt data into the database. Otherwise, immediately returns the found db record.
     *
     * @param productId the product id purchased by the user, e.g. `com.namazustudios.example_app.pack_10_coins`.
     * @param token the token issued to the user upon successful Google Play purchase transaction
     * @return the {@link GooglePlayIapReceipt} as it was written into the database, or the existing database record
     */
    List<GooglePlayIapReceipt> verifyAndCreateGooglePlayIapReceiptsIfNeeded(
            String productId,
            String token);

    /**
     * Gets or creates a {@link RewardIssuance} in the db for each given {@link GooglePlayIapReceipt}.
     *
     * @param googlePlayIapReceipts
     * @return
     */
    List<RewardIssuance> getOrCreateRewardIssuances(List<GooglePlayIapReceipt> googlePlayIapReceipts);
}
