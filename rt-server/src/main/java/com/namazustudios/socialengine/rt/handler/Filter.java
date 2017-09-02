package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Response;

import java.util.Iterator;
import java.util.function.Consumer;

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
     * The default initial filter, which does nothing but pass on to the next filter in the chain.
     */
    Filter INITIAL = (next, session, request, responseReceiver) -> next.next(session, request, responseReceiver);

    /**
     * This method implements the actual business logic of the filter.
     *
     * @param next the next filter
     * @param session the session
     * @param request the request
     *
     */
    void filter(Chain next, Session session, Request request, Consumer<Response> responseReceiver);

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
        void next(Session session, Request request, Consumer<Response> responseReceiver);

        /**
         * Builds a {@link Chain} from the supplied {@link Iterable<Filter>}.  Each time the
         * {@link Chain#next(Session, Request, Consumer<Response>)} method is invoked the supplied
         * {@link Iterable<Filter>} is walked until the end of the {@link Chain}, or until a
         * {@link Filter} in the chain refuses to foward the {@link Request}.
         *
         * @param filters the {@link Iterable<Filter>} instance
         * @return a {@link Chain} which walks the supplied {@link Iterable<Filter>}
         */
        static Chain build(final Iterable<Filter> filters) {
            return (session, request, responseReceiver) -> {

                final Iterator<Filter> iterator = filters.iterator();

                final Chain intermediate = new Chain() {
                    @Override
                    public void next(Session session, Request request, Consumer<Response> responseReceiver) {
                        if (iterator.hasNext()) {
                            final Filter filter = iterator.next();
                            filter.filter(this, session, request, responseReceiver);
                        }
                    }
                };

                INITIAL.filter(intermediate, session, request, responseReceiver);

            };
        }

    }

}
