package com.namazustudios.socialengine.service.appleiap;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import com.namazustudios.socialengine.model.mission.Mission;

import java.util.Set;

public interface AppleIapReceiptService {

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param user the user
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link Mission} objects.
     */
    Pagination<AppleIapReceipt> getAppleIapReceipts(User user, int offset, int count);

    /**
     * Gets the receipt with the id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param originalTransactionIdentifier the original apple transaction id
     * @return the {@link AppleIapReceipt} that was requested, never null
     */
    AppleIapReceipt getAppleIapReceipt(String originalTransactionIdentifier);

    /**
     * Creates a new receipt.
     *
     * @return the {@link AppleIapReceipt} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in AppleIapReceipt is invalid
     * @throws DuplicateException
     *     if the passed in AppleIapReceipt has a name that already exists
     */
    AppleIapReceipt createAppleIapReceipt(AppleIapReceipt appleIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param originalTransactionIdentifier the original apple transaction id
     */
    void deleteAppleIapReceipt(String originalTransactionIdentifier);

}
