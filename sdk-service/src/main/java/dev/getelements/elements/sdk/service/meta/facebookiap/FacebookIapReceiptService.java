package dev.getelements.elements.sdk.service.meta.facebookiap;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.meta.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapVerifyReceiptResponse;

import java.util.List;

@ElementPublic
@ElementServiceExport
public interface FacebookIapReceiptService {

    String FACEBOOK_IAP_SCHEME = "com.facebook.platform";

    String FACEBOOK_IAP_ROOT_URL = "https://graph.facebook.com/v20.0";

    String FACEBOOK_IAP_RECEIPT_CREATED = "dev.getelements.elements.sdk.service.receipt.meta.facebook.created";

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
     *     if the state of the passed in OculusIapReceipt is invalid
     * @throws DuplicateException
     *     if the passed in OculusIapReceipt has a name that already exists
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
    List<RewardIssuance> verifyAndCreateFacebookIapReceiptIfNeeded(FacebookIapReceipt receiptData);

}
