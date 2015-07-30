package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidParameterException;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The siple implementation of the {@link Server} inteface.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class SimpleServer implements Server {

    @Inject
    private PathHandlerService pathHandlerService;

    @Inject
    private ConnectedClientService connectedClientService;

    @Inject
    private ExceptionMapper.Resolver exceptionMapperResolver;

    private Filter.Chain filterChain;

    @Override
    public void handleRequest(final Client client, final Request request) {
        try {
            filterChain.next(client, request);
        } catch (Exception ex) {
            handleExceptions(ex, client, request);
        }
    }

    private void resolvePathHandlerAndDispatchRequest(final Client client, final Request<?> request) {

        final PathHandler<Object> pathHandler = pathHandlerService.getPathHandler(request.getHeader());

        final AtomicBoolean received = new AtomicBoolean();

        final Receiver<ResponseHeader, Object> receiver = new Receiver<ResponseHeader, Object>() {

            @Override
            public void receive(ResponseHeader header, Object payload) {

                if (received.compareAndSet(false, true)) {
                    final Receiver<ResponseHeader, Object> receiver;
                    receiver = connectedClientService.getResponseReceiver(client, Object.class);
                    receiver.receive(header, payload);
                } else {
                    throw new InternalException("Response already set.");
                }

            }
        };

        if (request.getPayload() == null) {

            pathHandler.handle(null, receiver);

            if (!received.compareAndSet(false, true)) {
                throw new InternalException("No response generated.");
            }

        } else if (pathHandler.getClass().isAssignableFrom(request.getPayload().getClass())) {
            // The generics get a little screwy, but the interface demands that the
            // path handler return an instance compatible with the given type, so we just
            // force the cast anyhow.
            pathHandler.handle((Request<Object>)request, receiver);
        } else {
            throw new InvalidParameterException("Expected instance of " +
                                                 pathHandler.getPayloadType() + " but got " +
                                                 request.getPayload().getClass() + " instead.");
        }

    }

    private <T extends Exception> void handleExceptions(final T ex, final Client client, final Request<Object> request) {

        final ExceptionMapper<T> exceptionMapper = (ExceptionMapper<T>)exceptionMapperResolver.getExceptionMapper(ex);


        final Receiver<ResponseHeader, Object> receiver;
        receiver = connectedClientService.getResponseReceiver(client, Object.class);

        if (exceptionMapper == null) {
            handleExceptionInternal(ex, client, request);
        } else {
            try {
                final Response<?> response = exceptionMapper.map(ex);
                receiver.receive(response.getResponseHeader(), response.getPayload());
            } catch (Exception _ex) {
                handleExceptionInternal(ex, client, request);
            }
        }

    }

    private void handleExceptionInternal(final Exception ex, final Client client, final Request<Object> request) {

    }

    @Inject
    void buildFilterChain(final List<Filter> filterList) {

        Collections.reverse(filterList);

        Filter.Chain chain = new Filter.Chain() {
            @Override
            public void next(Client client, Request<?> request) {
                resolvePathHandlerAndDispatchRequest(client, request);
            }
        };

        for (final Filter filter : filterList) {

            final Filter.Chain next = chain;

            chain = new Filter.Chain() {
                @Override
                public void next(Client client, Request<?> request) {
                    filter.filter(next, client, request);
                }
            };

        }

        filterChain = chain;

    }

}
