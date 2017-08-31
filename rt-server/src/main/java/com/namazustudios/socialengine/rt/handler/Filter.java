package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;

/**
 * Used to filer requests or maniuplate requests before they ultimately arrive
 * in an instance of {@link ClientRequestHandler}.
 *
 * Note that for the sake of performance, the filter is only available for use
 * by {@link Handler} instances.
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface Filter {

    /**
     * This method implements the actual business logic of the filter.
     *
     * @param chain the chain
     * @param session the session
     * @param request the request
     *
     */
    void filter(Chain chain, Session session, Request request, ResponseReceiver responseReceiver);

    /**
     * Represents the next filter in the chain of filters.
     */
    interface Chain {

        /**
         * Hands processing to the next filter in the chain.
         *
         * @param session the session the session
         * @param request the request the request
         *
         */
        void next(Session session, Request request, ResponseReceiver responseReceiver);

    }

}
