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

    /**
     * The receipt data key in the request for iOS Purchase validation.
     */
    String RECEIPT_DATA_KEY = "receipt-data";

    /**
     * Status code in the JSON response indicating that the receipt given in the request is valid.
     */
    int VALID_STATUS_CODE = 0;

    /**
     * Status code indicating that the IAP environment is incorrect.  That is, a production receipt was passed to a
     * sandbox URL.  This is used to signal a re-try of validation to avoid having to make iOS/Bundle changes.
     *
     * {@see https://developer.apple.com/documentation/storekit/in-app_purchase/validating_receipts_with_the_app_store?language=objc}
     *
     */
    int USE_TEST_INSTEAD = 21007;

    /**
     * Status code indicating that the IAP environment is incorrect.  That is, a sandbox receipt was passed to a
     * production URL.  This is used to signal a re-try of validation to avoid having to make iOS/Bundle changes.
     *
     * {@see https://developer.apple.com/documentation/storekit/in-app_purchase/validating_receipts_with_the_app_store?language=objc}
     *
     */
    int USE_PROD_INSTEAD = 21008;

}
