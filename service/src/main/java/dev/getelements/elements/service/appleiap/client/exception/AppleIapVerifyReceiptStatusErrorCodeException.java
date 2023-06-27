package dev.getelements.elements.service.appleiap.client.exception;

import dev.getelements.elements.exception.BaseException;
import dev.getelements.elements.exception.ErrorCode;

/**
 * Used to indicate that the Apple verify receipt API returned a valid JSON response but with an error "status" code in
 * the response body.
 */
public class AppleIapVerifyReceiptStatusErrorCodeException extends BaseException {

    private final int statusCode;

    public AppleIapVerifyReceiptStatusErrorCodeException(final int statusCode) {
        super("Received Apple IAP receipt verification status error code: " + statusCode);
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
