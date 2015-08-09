package com.namazustudios.socialengine.rt;

/**
 * Used to filer requests or maniuplate requests before they ultimately arrive
 * in an instance of {@link RequestPathHandler}.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface Filter {

    /**
     * This method implements the actual business logic of the filter.
     *
     * @param chain the chain
     * @param client the client
     * @param request the request
     *
     */
    void filter(Chain chain, Client client, Request request, ConnectedClientService.ResponseReceiver responseReceiver);

    /**
     * Represents the next filter in the chain of filters.
     */
    interface Chain {

        /**
         * Hands processing to the next filter in the chain.
         *
         * @param client the client the client
         * @param request the request the request
         *
         */
        void next(Client client, Request request, ConnectedClientService.ResponseReceiver responseReceiver);

    }

}
