package dev.getelements.elements.sdk.service.appleiap.client.exception;

import dev.getelements.elements.sdk.model.exception.BaseException;
import dev.getelements.elements.sdk.model.exception.ErrorCode;

import java.util.Map;

/**
 * Used to indicate that the Apple verify receipt API returned a valid JSON response but with an error "status" code in
 * the response body.
 *
 * <p>The message carries Apple's documented description for the status code alongside the number, so the log
 * names the actual failure rather than a bare integer. Descriptions come from Apple's StoreKit documentation
 * (Validating receipts with the App Store, response status table).
 */
public class AppleIapVerifyReceiptStatusErrorCodeException extends BaseException {

    /**
     * Apple's documented verifyReceipt response statuses and their meanings. Used to describe the failure in
     * the exception message.
     *
     * @see <a href="https://developer.apple.com/documentation/storekit/validating_receipts_with_the_app_store">Validating receipts with the App Store</a>
     */
    private static final Map<Integer, String> STATUS_DESCRIPTIONS = Map.ofEntries(
            Map.entry(21000, "the App Store could not read the JSON object provided"),
            Map.entry(21002, "the receipt data was malformed"),
            Map.entry(21003, "the receipt could not be authenticated"),
            Map.entry(21004, "the shared secret did not match the shared secret on file"),
            Map.entry(21005, "the receipt server was temporarily unable to provide the service"),
            Map.entry(21006, "the receipt is valid but the subscription has expired"),
            Map.entry(21007, "the receipt is from the test environment, but was sent to the production environment"),
            Map.entry(21008, "the receipt is from the production environment, but was sent to the test environment"),
            Map.entry(21009, "internal data access error"),
            Map.entry(21010, "the user account cannot be found or has been deleted")
    );

    private final int statusCode;

    public AppleIapVerifyReceiptStatusErrorCodeException(final int statusCode) {

        super("Received Apple IAP receipt verification status error code: " + statusCode
                + describe(statusCode));

        this.statusCode = statusCode;
    }

    private static String describe(final int statusCode) {
        final var description = STATUS_DESCRIPTIONS.get(statusCode);
        return description == null ? "" : " (" + description + ")";
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.EXTERNAL_RESOURCE_FAILED;
    }
}
