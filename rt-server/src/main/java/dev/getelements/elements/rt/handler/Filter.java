package dev.getelements.elements.rt.handler;

import dev.getelements.elements.rt.Request;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.Response;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Used to filer, modify, or intercept {@link Request} instances before being processed by the underlying
 * {@link Resource} instances.  The {@link Request} instances are handed through a {@link Chain} where they
 * execute various {@link Filter}s in sequence before they are finally processed by the destination
 * {@link Resource}.
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
        static Chain build(final Iterable<Filter> filters, final Chain terminal) {
            return (session, request, responseReceiver) -> {

                final Iterator<Filter> iterator = filters.iterator();

                final Chain intermediate = new Chain() {
                    @Override
                    public void next(Session session, Request request, Consumer<Response> responseReceiver) {
                        if (iterator.hasNext()) {
                            final Filter filter = iterator.next();
                            filter.filter(this, session, request, responseReceiver);
                        } else {
                            terminal.next(session, request, responseReceiver);
                        }
                    }
                };

                INITIAL.filter(intermediate, session, request, responseReceiver);

            };
        }

        /**
         * A container-supplied builder for {@link Filter.Chain} instances.  This allows for the dispatch of
         * {@link Request} instances through a chain of filters.
         */
        interface Builder {

            /**
             * Adds an additional {@link Filter} to this {@link Builder}
             *
             * @param filter
             * @return this instance
             */
            Builder withFilter(Filter filter);

            /**
             * Adds multiple {@link Filter}s to this {@link Builder}
             *
             * @param filters the {@link Filter}s to add
             * @return
             */
            Builder withFilters(Iterable<Filter> filters);

            /**
             * Terminates the {@link Chain} and returns a {@link Chain} which can be used to process ths {@link Filter}s
             * specified in this {@link Builder}.  The terminal link in the chain will then be used to finally dispatch
             * the request to the code which processes the {@link Request}.
             *
             * @param terminal the terminal {@link Filter}
             *
             * @return the {@link Chain}
             */
            Chain terminate(Chain terminal);

        }

    }

}
