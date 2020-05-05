package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import com.namazustudios.socialengine.rt.annotation.Expose;

@Expose(modules = {
        "namazu.elements.dao.appleiapreceipt",
        "namazu.socialengine.dao.appleiapreceipt",
})
public interface AppleIapReceiptDao {

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param user the user
     * @param offset the offset
     * @param count the count
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
     * @throws InvalidDataException
     *     if the state of the passed in AppleIapReceipt is invalid
     * @throws DuplicateException
     *     if the passed in AppleIapReceipt has a name that already exists
     */
    AppleIapReceipt getOrCreateAppleIapReceipt(AppleIapReceipt appleIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param originalTransactionId the original apple transaction id
     */
    void deleteAppleIapReceipt(String originalTransactionId);

}
