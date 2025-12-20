package dev.getelements.elements.sdk.service.meta.facebookiap.client.invoker;

import dev.getelements.elements.sdk.model.meta.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapVerifyReceiptResponse;

public interface FacebookIapReceiptRequestInvoker {

    /**
     * Sends a request to Facebook to verify the receipt
     * @param receipt data to verify
     * @return the verification response
     */
    FacebookIapVerifyReceiptResponse invokeVerify(FacebookIapReceipt receipt, String appId, String appSecret);

}
