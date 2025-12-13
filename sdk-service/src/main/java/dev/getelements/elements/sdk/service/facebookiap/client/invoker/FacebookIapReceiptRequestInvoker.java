package dev.getelements.elements.sdk.service.facebookiap.client.invoker;

import dev.getelements.elements.sdk.model.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.service.facebookiap.client.model.FacebookIapConsumeResponse;
import dev.getelements.elements.sdk.service.facebookiap.client.model.FacebookIapVerifyReceiptResponse;

public interface FacebookIapReceiptRequestInvoker {

    /**
     * Sends a request to Facebook to verify the receipt
     * @param receipt data to verify
     * @return the verification response
     */
    FacebookIapVerifyReceiptResponse invokeVerify(FacebookIapReceipt receipt, String appId, String appSecret);

    /**
     * Sends a request to Facebook to consume the receipt
     * @param receipt data to consume
     * @return the consume response
     */
    FacebookIapConsumeResponse invokeConsume(FacebookIapReceipt receipt, String appId, String appSecret);
}
