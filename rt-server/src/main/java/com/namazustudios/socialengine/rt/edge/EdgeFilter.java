package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.rt.Client;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;
import com.namazustudios.socialengine.rt.edge.EdgeResponseReceiver;

/**
 * Used to filer requests or maniuplate requests before they ultimately arrive
 * in an instance of {@link EdgeRequestPathHandler}.
 *
 * Note that for the sake of performance, the filter is only available for use
 * by {@link EdgeResource} instances.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface EdgeFilter {

    /**
     * This method implements the actual business logic of the filter.
     *
     * @param chain the chain
     * @param client the client
     * @param request the request
     *
     */
    void filter(Chain chain, Client client, Request request, EdgeResponseReceiver edgeResponseReceiver);

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
        void next(Client client, Request request, EdgeResponseReceiver edgeResponseReceiver);

    }

}
