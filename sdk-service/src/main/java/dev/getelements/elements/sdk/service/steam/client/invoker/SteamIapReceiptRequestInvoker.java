package dev.getelements.elements.sdk.service.steam.client.invoker;

import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;

public interface SteamIapReceiptRequestInvoker {

    /**
     * Queries the Steam ISteamMicroTxn API (or the ISteamMicroTxnSandbox API when sandbox is true)
     * for the status of a transaction by order ID.
     *
     * @param publisherKey the Steam Publisher Web API Key
     * @param appId the Steam AppID
     * @param orderId the Steam order ID to query
     * @param sandbox true to query the ISteamMicroTxnSandbox endpoint (development/team purchases)
     * @return the query response from Steam
     */
    SteamIapQueryTxnResponse invokeQueryTxn(String publisherKey, String appId, String orderId, boolean sandbox);

}
