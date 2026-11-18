package dev.getelements.elements.service.steam.invoker;

import dev.getelements.elements.rt.exception.InternalException;

/**
 * Thrown when Steam's micro-transaction API responds with a non-OK status to a QueryTxn call. The message
 * carries the queried endpoint (live or sandbox), the HTTP status, and Steam's response body, which names
 * the actual failure: an order not found in this environment, a key without access to the app, an app not
 * entitled for micro-transactions, and so on.
 */
public class SteamIapQueryTxnException extends InternalException {

    public SteamIapQueryTxnException(final String message) {
        super(message);
    }

    public SteamIapQueryTxnException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
