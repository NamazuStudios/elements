package dev.getelements.elements.sdk.service.meta.oculusiap.client.exception;

import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.ErrorCode;

/**
 * Used to indicate that the Apple verify receipt API returned a valid JSON response but with an error "status" code in
 * the response body.
 */
public class OculusIapVerifyReceiptStatusErrorCodeException extends BaseException {

    private final int statusCode;

    public OculusIapVerifyReceiptStatusErrorCodeException(final int statusCode) {
        super("Received Oculus IAP receipt verification status error code: " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.EXTERNAL_RESOURCE_FAILED;
    }
}
