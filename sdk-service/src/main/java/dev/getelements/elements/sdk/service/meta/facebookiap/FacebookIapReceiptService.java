package dev.getelements.elements.sdk.service.facebookiap;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.facebookiap.client.model.FacebookIapConsumeResponse;
import dev.getelements.elements.sdk.service.facebookiap.client.model.FacebookIapVerifyReceiptResponse;

import java.util.List;

@ElementPublic
@ElementServiceExport
public interface FacebookIapReceiptService {

    String OCULUS_PLATFORM_IAP_SCHEME = "com.oculus.platform";

    String OCULUS_ROOT_URL = "https://graph.oculus.com";

    String OCULUS_RECEIPT_CREATED = "dev.getelements.elements.sdk.service.receipt.google.play.created";

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link FacebookIapReceipt} objects.
     */
    Pagination<FacebookIapReceipt> getFacebookIapReceipts(int offset, int count);

    /**
     * Gets the receipt with the id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param originalTransactionId the original facebook transaction id
     * @return the {@link Receipt} that was requested, never null
     */
    FacebookIapReceipt getFacebookIapReceipt(String originalTransactionId);

    /**
     * Finds a receipt in the db, or creates a new one if necessary.
     *
     * @return the {@link Receipt} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in FacebookIapReceipt is invalid
     * @throws DuplicateException
     *     if the passed in FacebookIapReceipt has a name that already exists
     */
    FacebookIapReceipt getOrCreateFacebookIapReceipt(FacebookIapReceipt facebookIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param originalTransactionId the original facebook transaction id
     */
    void deleteFacebookIapReceipt(String originalTransactionId);

    /**
     * Verifies the given base64-encoded receiptData string against the Facebook servers in the given environment.
     *
     * @param receiptData
     * @return the {@link FacebookIapReceipt} as it was written into the database, or the existing database record
     */
    FacebookIapVerifyReceiptResponse verifyAndCreateFacebookIapReceiptIfNeeded(FacebookIapReceipt receiptData);

    /**
     * Verifies the given receipt against the Facebook servers.
     *
     * @param receiptData
     * @return the {@link FacebookIapReceipt} as it was written into the database, or the existing database record
     */
    FacebookIapConsumeResponse consumeAndRecordFacebookIapReceipt(FacebookIapReceipt receiptData);


    /**
     * Gets or creates {@link RewardIssuance} in the db for the given {@link FacebookIapReceipt}.
     *
     * @param facebookIapReceipt
     * @return the reward issuances from the product bundle matching the receipt sku
     */
    List<RewardIssuance> getOrCreateRewardIssuances(FacebookIapReceipt facebookIapReceipt);
}
