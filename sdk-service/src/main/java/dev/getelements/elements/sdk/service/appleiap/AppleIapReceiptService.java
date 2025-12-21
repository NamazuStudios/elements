package dev.getelements.elements.sdk.service.appleiap;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment;

import java.util.List;

@ElementPublic
@ElementServiceExport
@ElementEventProducer(
        value = AppleIapReceiptService.APPLE_IAP_RECEIPT_CREATED,
        parameters = AppleIapReceipt.class,
        description = "Called when a new Google Play receipt is created."
)
public interface AppleIapReceiptService {

    String APPLE_IAP_SCHEME = "com.apple.appstore";

    String APPLE_IAP_RECEIPT_CREATED = "dev.getelements.elements.sdk.service.receipt.apple.appstore.created";

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
