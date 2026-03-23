package dev.getelements.elements.sdk.service.steam.client.invoker;

import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;

public interface SteamIapReceiptRequestInvoker {

    /**
     * Queries the Steam ISteamMicroTxn API for the status of a transaction by order ID.
     *
     * @param publisherKey the Steam Publisher Web API Key
     * @param appId the Steam AppID
     * @param orderId the Steam order ID to query
     * @return the query response from Steam
     */
    SteamIapQueryTxnResponse invokeQueryTxn(String publisherKey, String appId, String orderId);

}
