package dev.getelements.elements.service.appleiap;

import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.model.reward.RewardIssuance;
import dev.getelements.elements.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment;

import java.util.List;

public interface AppleIapReceiptService {

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
     * Finds a receipt in the db, or creates a new one if necessary.
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

    /**
     * Verifies the given base64-encoded receiptData string against the Apple servers in the given environment.
     *
     * @param appleIapVerifyReceiptEnvironment
     * @param receiptData
     * @return the {@link AppleIapReceipt} as it was written into the database, or the existing database record
     */
    List<AppleIapReceipt> verifyAndCreateAppleIapReceiptsIfNeeded(
            AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment,
            String receiptData);

    /**
     * Gets or creates a {@link RewardIssuance} in the db for each given {@link AppleIapReceipt}.
     *
     * @param appleIapReceipts
     * @return
     */
    List<RewardIssuance> getOrCreateRewardIssuances(List<AppleIapReceipt> appleIapReceipts);
}
