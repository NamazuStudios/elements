package com.namazustudios.socialengine;


public interface AppleIapConstants {
    /**
     * The base url for sandbox receipt verification.
     */
    String SANDBOX_BASE_API_URL = "https://sandbox.itunes.apple.com";

    /**
     * The base url for production receipt verification.
     */
    String PRODUCTION_BASE_API_URL = "https://buy.itunes.apple.com";

    /**
     * The path for the verify receipt endpoint.
     */
    String VERIFY_RECEIPT_PATH_COMPONENT = "verifyReceipt";

    String RECEIPT_DATA_KEY = "receipt-data";

    /**
     * Status code in the JSON response indicating that the receipt given in the request is valid.
     */
    int VALID_STATUS_CODE = 0;
}
