package dev.getelements.elements.sdk.service.meta.oculusiap.client.invoker;

import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;

public interface OculusIapReceiptRequestInvoker {

    /**
     * Sends a request to Oculus to verify the receipt
     * @param receipt data to verify
     * @return the verification response
     */
    OculusIapVerifyReceiptResponse invokeVerify(OculusIapReceipt receipt, String appId, String appSecret);

    /**
     * Sends a request to Oculus to consume the receipt
     * @param receipt data to consume
     * @return the consume response
     */
    OculusIapConsumeResponse invokeConsume(OculusIapReceipt receipt, String appId, String appSecret);
}
