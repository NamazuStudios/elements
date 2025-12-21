package dev.getelements.elements.sdk.service.meta.oculusiap.client.exception;

import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.ErrorCode;

/**
 * Used to indicate that the Apple verify receipt API returned a valid JSON response but with an error "status" code in
 * the response body.
 */
public class OculusIapVerifyReceiptGraphErrorException extends BaseException {

    private final String graphError;

    public OculusIapVerifyReceiptGraphErrorException(final String graphError) {
        super("Received Oculus IAP receipt verification status error: " + graphError);
        this.graphError = graphError;
    }

    public String getStatusCode() {
        return graphError;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.EXTERNAL_RESOURCE_FAILED;
    }
}
