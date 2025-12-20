package dev.getelements.elements.sdk.service.meta.oculusiap;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;

import java.util.List;

@ElementPublic
@ElementServiceExport
@ElementEventProducer(
        value = OculusIapReceiptService.OCULUS_IAP_RECEIPT_CREATED,
        parameters = OculusIapReceipt.class,
        description = "Called when a new Google Play receipt is created."
)
public interface OculusIapReceiptService {

    String OCULUS_IAP_SCHEME = "com.oculus.platform";

    String OCULUS_IAP_ROOT_URL = "https://graph.oculus.com";

    String OCULUS_IAP_RECEIPT_CREATED = "dev.getelements.elements.sdk.service.receipt.meta.oculus.created";

    /**
     * Gets receipts for a given user specifying the offset and the count.
     *
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link OculusIapReceipt} objects.
     */
    Pagination<OculusIapReceipt> getOculusIapReceipts(int offset, int count);

    /**
     * Gets the receipt with the id, or throws a {@link NotFoundException} if the
     * receipt can't be found.
     *
     * @param originalTransactionId the original oculus transaction id
     * @return the {@link Receipt} that was requested, never null
     */
    OculusIapReceipt getOculusIapReceipt(String originalTransactionId);

    /**
     * Finds a receipt in the db, or creates a new one if necessary.
     *
     * @return the {@link Receipt} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in OculusIapReceipt is invalid
     * @throws DuplicateException
     *     if the passed in OculusIapReceipt has a name that already exists
     */
    OculusIapReceipt getOrCreateOculusIapReceipt(OculusIapReceipt oculusIapReceipt);

    /**
     * Deletes an existing receipt.
     *
     * @param originalTransactionId the original oculus transaction id
     */
    void deleteOculusIapReceipt(String originalTransactionId);

    /**
     * Verifies the given base64-encoded receiptData string against the Oculus servers in the given environment.
     *
     * @param receiptData
     * @return the {@link OculusIapReceipt} as it was written into the database, or the existing database record
     */
    OculusIapVerifyReceiptResponse verifyAndCreateOculusIapReceiptIfNeeded(OculusIapReceipt receiptData);

    /**
     * Verifies the given receipt against the Oculus servers.
     *
     * @param receiptData
     * @return the {@link OculusIapReceipt} as it was written into the database, or the existing database record
     */
    OculusIapConsumeResponse consumeAndRecordOculusIapReceipt(OculusIapReceipt receiptData);


    /**
     * Gets or creates {@link RewardIssuance} in the db for the given {@link OculusIapReceipt}.
     *
     * @param oculusIapReceipt
     * @return the reward issuances from the product bundle matching the receipt sku
     */
    List<RewardIssuance> getOrCreateRewardIssuances(OculusIapReceipt oculusIapReceipt);
}
