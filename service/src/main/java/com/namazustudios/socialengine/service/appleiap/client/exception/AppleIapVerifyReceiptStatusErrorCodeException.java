package com.namazustudios.socialengine.service.appleiap.client.exception;

import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.service.gameon.client.model.ErrorResponse;

import javax.ws.rs.core.Response;

/**
 * Used to indicate that the Apple verify receipt API returned a valid JSON response but with an error "status" code in
 * the response body.
 */
public class AppleIapVerifyReceiptStatusErrorCodeException extends RuntimeException {

    private final int statusCode;

    public AppleIapVerifyReceiptStatusErrorCodeException(final int statusCode) {
        super("Received Apple IAP receipt verification status error code: " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
