package com.namazustudios.socialengine.service.appleiap.client.invoker;

import com.namazustudios.socialengine.model.gameon.game.GameOnEnterMatchResponse;
import com.namazustudios.socialengine.model.gameon.game.*;
import com.namazustudios.socialengine.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import com.namazustudios.socialengine.service.appleiap.client.model.AppleIapVerifyReceiptResponse;
import com.namazustudios.socialengine.service.gameon.client.model.EnterMatchRequest;

public interface AppleIapVerifyReceiptInvoker {
    AppleIapGrandUnifiedReceipt invoke();

    enum AppleIapVerifyReceiptEnvironment {
        /**
         * The Sandbox environment (i.e. https://sandbox.itunes.apple.com/verifyReceipt).
         */
        SANDBOX,

        /**
         * The Production environment (i.e. https://buy.itunes.apple.com/verifyReceipt).
         */
        PRODUCTION
    }

    interface Builder {
        /**
         * Specifies the base64-encoded receipt string to be verified against the given
         * {@link AppleIapVerifyReceiptEnvironment}.
         *
         * @param appleIapVerifyReceiptEnvironment The desired Apple environment to point to for the request.
         * @param receiptData The base64-encoded receipt string to be verified.
         * @return this instance
         */
        Builder withReceiptData(AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment,
                                String receiptData);

        /**
         * Builds the instance of {@link AppleIapVerifyReceiptInvoker}.
         *
         * @return the newly created {@link AppleIapVerifyReceiptInvoker}
         */
        AppleIapVerifyReceiptInvoker build();
    }
}
